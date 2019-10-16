package io.github.erdos.bellang.eval;

import io.github.erdos.bellang.objects.Character;
import io.github.erdos.bellang.objects.Expression;
import io.github.erdos.bellang.objects.ExpressionVisitor;
import io.github.erdos.bellang.objects.Pair;
import io.github.erdos.bellang.objects.Stream;
import io.github.erdos.bellang.objects.Symbol;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static io.github.erdos.bellang.eval.EvaluationException.evalException;
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

	// see: ev function in bel.bel
	public Expression appliedTo(Expression param) {
		return param.apply(this);
	}

	@Override
	public Expression pair(Pair pair) {

		Optional<Variable> maybeVar = Variable.of(pair);
		if (maybeVar.isPresent()) {
			return env.get(maybeVar.get()).orElseThrow(evalException(pair, "Could not resolve variable!"));
		}

		Expression sym = pair.car();

		// TODO: call order:

		// TODO: maybe remove it!
		if (Symbol.symbol("err") == sym) {
			Expression expression = appliedTo(pair.cadr());

			if (expression != NIL) throw new EvaluationException(expression, "Err called!");

			return expression;
		}

		if (Symbol.LIT.equals(sym)) {
			return specialForms.evalLit(pair);
		}

		if (Symbol.IF.equals(sym)) {
			return specialForms.evalIf(pair, this);
		}

		if (Symbol.QUOTE.equals(sym)) {
			return specialForms.evalQuote(pair);
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

		// System.out.println("Evaling lit call: " + expression + " with " + env.getLexicalScope());

		Expression head = expression.car().apply(this);

		if (!(head instanceof Pair)) {
			throw new EvaluationException(head, "Expected literal expression, instead we got: " + head + " original expression was; " + expression + " scope=" + env.getLexicalScope() + " ");
		}

		if (((Pair) head).cadr() == Symbol.MAC) {
			Pair nestedClo = (Pair) ((Pair) head).caddr(); // lit inside mac!
			try {
				Expression macroCallResult = evalFnCallImpl(nestedClo, expression.cdr(), x -> x, "");
				return this.appliedTo(macroCallResult);
			} catch (IllegalArgumentException e) {
				System.out.println("Original full expression: " + expression);
				throw e;
			}
		} else if (((Pair) head).cadr() == Symbol.CLO) {
			return evalFnCallImpl((Pair) head, expression.cdr(), this::appliedTo, "expression=" + expression + " lexicalScope=" + env.getLexicalScope());
		} else {
			throw new IllegalArgumentException("We only evaluate MAC or CLO literals!");
		}
	}

	private Expression evalFnCallImpl(Pair fn, Expression passedParamValues, Function<Expression, Expression> argsMapper, String debug) {
		assert fn.car() == LIT;
		assert fn.cadr() == Symbol.CLO;

		Expression paramDeclarations = fn.cadddr(); // fourth elem

		Expression body = fn.caddddr();

		final Map<Variable, Expression> newScope = mapArgs(paramDeclarations, passedParamValues, argsMapper, debug);

		if (fn.caddr() != NIL) { // local closure
			((Pair) fn.caddr()).forEach(binding -> newScope.putIfAbsent(Variable.of( ((Pair) binding).car()).get(), ((Pair) binding).cdr()));
		}

		env.pushLexicals(newScope);
		try {
			return body.apply(this);
		} finally {
			env.popLexicals();
		}
	}

	private static void paramEvalIntoMap(Expression definition, Expression value, Map<Variable, Expression> targetMap, Function<Expression, Expression> mapper) {
		if (definition instanceof Symbol) {
			targetMap.put(Variable.of(definition).get(), map(value, mapper));
		} else if ( ((Pair)definition).car() == O) {
			// optional parameter
		} else if (((Pair) definition).car() == T) {
			// type checked parameter
		} else {
			// it is some destructure string?
		}
	}

	private static Map<Variable, Expression> mapArgs(Expression names, Expression values0, Function<Expression, Expression> mapper, String debug) {
		Map<Variable, Expression> result = new HashMap<>();
		final Expression names0 = names;

		Optional<Pair> values = values0 == NIL ? Optional.empty() : Optional.of((Pair) values0);

		// TODO: is it needed here?
		if (names.equals(RT.pair(NIL, NIL))) {
			// TODO: itt ellenorizni kene a values ertekeit is!
			return result;
		}

		while (names != NIL) {
			if (names instanceof Symbol) {
				// last item is just a symbol.
				if (values.isPresent()) {
					result.put(Variable.of(names).get(), map(values.get(), mapper));
				} else {
					result.put(Variable.of(names).get(), NIL);
				}
				break;
			} else {
				Pair name = (Pair) names;

				if ((name.car() instanceof Symbol)) {
					if (! values.isPresent()) {
						throw new EvaluationException(names, "Can not call with less arguments than expected! " + debug + " param names=" + names0);
					} else {
						result.put(Variable.of(name.car()).get(), mapper.apply(values.get().car()));
					}
				} else {
					// optional parameter
					Pair clause = (Pair) name.car();

					if (clause.car() == O) {
						// itt a masodik parameter a valtozo. ezt rekurzivan kene feldolgozni.
						final Variable paramName = Variable.of(clause.nth(1))
								.orElseThrow(evalException(clause.nth(1), "Could not resolve variable" + names));

						final Expression defaultValue = clause.nthOrNil(2);

						// optional parameter
						if (values.isPresent()) {
							result.put(paramName, mapper.apply(values.get().car()));
						} else {
							result.put(paramName, mapper.apply(defaultValue));
						}
					} else if (clause.car() == T) {
						// TODO: how about t values?
						// throw new IllegalArgumentException("T values are not supported (yet)!");

						// TODO: apply variable testing here!!!
						// TODO: apply destructuring here!!!

						Variable paramName = Variable.of(name.cadr())
								.orElseThrow(evalException(NIL, "Not a valid variable!"));
						result.put(paramName, mapper.apply(values.get().car()));

					} else {
						if (! values.isPresent()) {
							throw new EvaluationException(names, "Can not call with less arguments than expected! " + debug + " param names=" + names0);
						} else {
							Variable paramName = Variable.of(name.car())
									.orElseThrow(evalException(name.car(), "Not a valid variable!"));
							result.put(paramName, mapper.apply(values.get().car()));
						}
					}
				}

				names = name.cdr();
				values = values.map(x -> x.cdr() == NIL ? null : (Pair) x.cdr());
			}
		}

		return result;
	}

	private static Expression map(Expression p, Function<Expression, Expression> f) {
		if (p == NIL) {
			return NIL;
		} else {            // TODO: remove recursion!
			Pair pair = (Pair) p;
			return RT.pair(f.apply(pair.car()), map(pair.cdr(), f));
		}
	}

	@Override
	public Expression stream(Stream stream) {
		throw new RuntimeException("Stream not supported yet!");
	}

	@Override
	public Expression symbol(Symbol symbol) {
		// TODO: lookup order: dynamic, scope, globe, defaults.

		if (symbol == T || symbol == NIL || symbol == O || symbol == APPLY) {
			return symbol;
		}
		//else if (symbol == JOIN) {
		// TODO: not sure if it is semantically correct.
		//	return symbol;
		//}

		Optional<Expression> bound = env.get(Variable.of(symbol).get());
		if (bound.isPresent()) return bound.get();

		if (symbol == CHARS) {
			return Constants.CHARS_LIST;
		} else if (symbol == GLOBE) {
			throw new RuntimeException("Can not use globe yet.");
		} else if (symbol == SCOPE) {
			Expression tail = NIL;
			for (Map.Entry<Variable, Expression> entry : env.getLexicalScope().entrySet()) {
				tail = RT.pair(RT.pair(entry.getKey().getExpression(), entry.getValue()), tail);
			}
			return tail;
		} else if (symbol == INS) {
			throw new RuntimeException("Can not use ins yet.");
		} else if (symbol == OUTS) {
			throw new RuntimeException("Can not use outs yet.");
		}

		throw new EvaluationException(symbol, "Symbol not bound: " + symbol);
	}

	@Override
	public Expression character(Character character) {
		return character;
	}

	Expression set(Pair call) {
		assert Symbol.SET == call.car();

		Expression tail = call.cdr();

		while (tail != NIL) {
			Pair pair = (Pair) tail;
			Symbol key = (Symbol) pair.car();
			Expression value = pair.cadr().apply(this);
			env.set(Variable.of(key).get(), value);

			tail = ((Pair)((Pair)tail).cdr()).cdr();
		}

		return NIL;
	}
}
