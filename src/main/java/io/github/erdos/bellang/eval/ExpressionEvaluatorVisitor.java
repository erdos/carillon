package io.github.erdos.bellang.eval;

import io.github.erdos.bellang.objects.Character;
import io.github.erdos.bellang.objects.Expression;
import io.github.erdos.bellang.objects.ExpressionVisitor;
import io.github.erdos.bellang.objects.Pair;
import io.github.erdos.bellang.objects.Stream;
import io.github.erdos.bellang.objects.Symbol;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.github.erdos.bellang.objects.Symbol.APPLY;
import static io.github.erdos.bellang.objects.Symbol.CHARS;
import static io.github.erdos.bellang.objects.Symbol.GLOBE;
import static io.github.erdos.bellang.objects.Symbol.INS;
import static io.github.erdos.bellang.objects.Symbol.LIT;
import static io.github.erdos.bellang.objects.Symbol.NIL;
import static io.github.erdos.bellang.objects.Symbol.O;
import static io.github.erdos.bellang.objects.Symbol.OUTS;
import static io.github.erdos.bellang.objects.Symbol.SCOPE;
import static io.github.erdos.bellang.objects.Symbol.T;

class ExpressionEvaluatorVisitor implements ExpressionVisitor<Expression> {
	private final Primitives primitives = new Primitives();
	private final SpecialForms specialForms = new SpecialForms();

	private final Evaluator env = new Evaluator();

	public Expression appliedTo(Expression param) {
		return param.apply(this);
	}

	@Override
	public Expression pair(Pair pair) {
		Expression sym = pair.car();

		if (Symbol.IF.equals(sym)) {
			return specialForms.evalIf(pair, this);
		}

		if (Symbol.QUOTE.equals(sym)) {
			return specialForms.evalQuote(pair);
		}

		if (Symbol.LIT.equals(sym)) {
			return specialForms.evalLit(pair);
		}

		if (Symbol.ID.equals(sym)) {
			return primitives.evalId(pair, this);
		}

		if (Symbol.APPLY.equals(sym)) {
			return specialForms.evalApply(pair, this);
		}

		if (Symbol.JOIN.equals(sym)) {
			return primitives.evalJoin(pair, this);
		}

		if (Symbol.CAR.equals(sym)) {
			return primitives.evalCar(pair, this);
		}

		if (Symbol.CDR.equals(sym)) {
			return primitives.evalCdr(pair, this);
		}

		if (Symbol.TYPE.equals(sym)) {
			return primitives.evalType(pair, this);
		}

		if (Symbol.XAR.equals(sym)) {
			return primitives.evalXar(pair, this);
		}

		if (Symbol.CXR.equals(sym)) {
			return primitives.evaCxr(pair, this);
		}

		if (Symbol.SYM.equals(sym)) {
			return primitives.evalSym(pair, this);
		}

		if (Symbol.NOM.equals(sym)) {
			return primitives.evalNom(pair, this);
		}

		if (Symbol.COIN.equals(sym)) {
			return primitives.coin(pair);
		}

		if (Symbol.SYS.equals(sym)) {
			return primitives.sys(pair, this);
		}

		if (Symbol.SET.equals(sym)) {
			return set(pair);
		}

		return evalLitCall(pair);
	}

	private Expression evalLitCall(Pair expression) {

		//System.out.println("Evaling lit call: " + expression + " with " + env.getLexicalScope());
		if (expression.isEmpty()) {
			throw new EvaluationException(expression, "Can not evaluate empty list!");
		}

		Expression head = this.appliedTo(expression.car());

		if (! (head instanceof Pair)) {
			throw new EvaluationException(head, "Expected literal expression, instead we got: " + head + " original expression was; " + expression + " scope=" + env.getLexicalScope() + " ");
		}

		if (((Pair) head).cadr() == Symbol.MAC) {
			// System.out.println("Evaluating macro " + head);
			Pair nestedClo = (Pair) ((Pair) head).caddr(); // lit inside mac!
			Expression macroCallResult = evalFnCallImpl(nestedClo, expression.cdr(), x -> x);
			return this.appliedTo(macroCallResult);

		} else if (((Pair) head).cadr() == Symbol.CLO) {
			return evalFnCallImpl((Pair) head, expression.cdr(), this::appliedTo);
		} else {
			throw new IllegalArgumentException("We only evaluate MAC or CLO literals!");
		}
	}

	private Expression evalFnCallImpl(Pair fn, Expression bodies, Function<Expression, Expression> argsFn) {
		assert fn.car() == LIT;
		assert fn.cadr() == Symbol.CLO;

		Expression params = fn.cadddr(); // fourth elem
		Expression body = fn.caddddr();

		if (bodies == NIL) {
			env.pushLexicals(mapArgsEmpty(params));
		} else {
			env.pushLexicals(mapArgs(params, (Pair) bodies, argsFn));
		}
		try {
			return appliedTo(body);
		} finally {
			env.popLexicals();
		}
	}

	private static Map<String, Expression> mapArgsEmpty(Expression names) {
		Map<String, Expression> result = new HashMap<>();
		while (names != NIL) {
			if (names instanceof  Symbol) {
				result.put(((Symbol) names).name, NIL);
				break;
			} else {
				result.put( ((Symbol) ((Pair)names).car()).name, NIL);
				names = ((Pair)names).cdr();
			}
		}
		return result;
	}

	private static Map<String, Expression> mapArgs(Expression names, Pair values, Function<Expression, Expression> mapper) {

		Map<String, Expression> result = new HashMap<>();

		while (names != NIL) {
			if (names instanceof Symbol) {
				result.put(((Symbol) names).name, map(values, mapper));
				break;
			} else {
				Pair name = (Pair) names;




				if ((name.car() instanceof Symbol)) {
					result.put(((Symbol)name.car()).name, mapper.apply(values.car()));
				} else {
					// optional parameter
					Pair clause = (Pair) name.car();

					assert clause.nth(0) == O;

					String paramName = ((Symbol)clause.nth(1)).name;
					Expression paramValue = mapper.apply(clause.nthOrNil(2));
					result.put(paramName, paramValue);
				}
				// System.out.println("Name is + " + name.car());

				if (values.cdr() == NIL) {
					while (name.cdr() != NIL) {
						if (name.cdr() instanceof Pair) {
							if ((name.cadr() instanceof Symbol)) {
								result.put(((Symbol) name.cadr()).name, NIL);

							} else {
								// optional parameter
								Pair clause = (Pair) name.cadr();

								assert clause.nth(0) == O;

								String paramName = ((Symbol)clause.nth(1)).name;
								Expression paramValue = mapper.apply(clause.nthOrNil(2));
								result.put(paramName, paramValue);
							}
							name = (Pair) name.cdr();
						} else {
							result.put(((Symbol) name.cdr()).name, NIL);
							break;
						}
					}
					break;
				} else {
					names = name.cdr();
					values = (Pair) values.cdr();
				}
			}
		}

		return result;
	}

	private static Expression map(Expression p, Function<Expression, Expression> f) {
		if (p == NIL) {
			return NIL;
		} else {			// TODO: remove recursion!
			Pair pair = (Pair) p;
			return RT.pair(f.apply(pair.car()), map(pair.cdr(), f));
		}
	}

	private static boolean isLit(Expression e) {
		return e instanceof Pair && ((Pair) e).car() == Symbol.LIT;
	}

	@Override
	public Expression stream(Stream stream) {
		throw new RuntimeException("Stream not supported yet!");
	}

	@Override
	public Expression symbol(Symbol symbol) {
		if (symbol == T || symbol == NIL || symbol == O || symbol == APPLY) {
			return symbol;
		}
		//else if (symbol == JOIN) {
			// TODO: not sure if it is semantically correct.
		//	return symbol;
		//}
		else if (symbol == CHARS) {
			return Constants.CHARS_LIST;
		} else if (symbol == GLOBE) {
			throw new RuntimeException("Can not use globe yet.");
		} else if (symbol == SCOPE) {
			Expression tail = NIL;
			for (Map.Entry<String, Expression> entry : env.getLexicalScope().entrySet()) {
				tail = RT.pair(RT.pair(Symbol.symbol(entry.getKey()), entry.getValue()), tail);
			}
			return tail;
		} else if (symbol == INS) {
			throw new RuntimeException("Can not use ins yet.");
		} else if (symbol == OUTS) {
			throw new RuntimeException("Can not use outs yet.");
		} else {
			return env.get(symbol);
		}
	}

	@Override
	public Expression character(Character character) {
		return character;
	}

	Expression set(Pair pair) {
		assert Symbol.SET == pair.car();

		Symbol key = (Symbol) pair.cadr();
		Expression value = pair.caddr().apply(this);

		env.set(key, value);

		return value;
	}
}
