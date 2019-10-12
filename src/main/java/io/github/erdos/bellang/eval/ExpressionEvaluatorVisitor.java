package io.github.erdos.bellang.eval;

import io.github.erdos.bellang.objects.Character;
import io.github.erdos.bellang.objects.Expression;
import io.github.erdos.bellang.objects.ExpressionVisitor;
import io.github.erdos.bellang.objects.Pair;
import io.github.erdos.bellang.objects.Stream;
import io.github.erdos.bellang.objects.Symbol;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.github.erdos.bellang.objects.Symbol.APPLY;
import static io.github.erdos.bellang.objects.Symbol.CHARS;
import static io.github.erdos.bellang.objects.Symbol.GLOBE;
import static io.github.erdos.bellang.objects.Symbol.INS;
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
		assert param != null;
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

		return evalFnCall(pair);
	}

	private Expression evalFnCall(Pair expression) {
		// TODO: sorban kiertekelunk mindent.
		// ha az elso elemn fn literal, akkor a body reszt evalualjuk frissitett bindingekkel.

		if (expression.isEmpty()) {
			throw new EvaluationException(expression, "Can not evaluate empty list!");
		}

		List<Expression> args = new LinkedList<>();

		Expression head = this.appliedTo(expression.car());
		if (!isLit(head)) throw new EvaluationException(args.get(0), "Not a function!");

		boolean macro =  ((Pair) head).cadr() == Symbol.MAC;

		Pair params = (Pair) ((Pair) head).cadddr();
		Expression body = ((Pair) head).caddddr();

		if (macro) {
			assert ((Pair) head).cadr() == Symbol.MAC;
			env.pushLexicals(mapArgs(params, (Pair) expression.cdr(), x->x));
		} else {
			assert ((Pair) head).cadr() == Symbol.CLO;
			env.pushLexicals(mapArgs(params, (Pair) expression.cdr(), this::appliedTo));
		}

		Expression result;
		try {
			result = appliedTo(body);
		} finally {
			env.popLexicals();
		}

		if (macro) {
			result = appliedTo(result);
		}
		return result;
	}

	private Map<String, Expression> mapArgs(Expression names, Pair values, Function<Expression, Expression> mapper) {

		Map<String, Expression> result = new HashMap<>();

		while (names != NIL) {
			if (names instanceof Symbol) {
				result.put(((Symbol) names).name, values);
			} else {
				Pair name = (Pair) names;
				result.put(((Symbol)name.car()).name, values.car());

				if (values.cdr() == NIL) {
					assert name.cdr() == NIL;
					break;
				} else {
					names = name.cdr();
					values = (Pair) values.cdr();
				}
			}
		}

		return result;
	}

	private static boolean isLit(Expression e) {
		return e instanceof Pair && ((Pair) e).car() == Symbol.LIT;
	}

	@Override
	public Expression stream(Stream stream) {
		return null;
	}

	@Override
	public Expression symbol(Symbol symbol) {
		if (symbol == T || symbol == NIL || symbol == O || symbol == APPLY) {
			return symbol;
		} else if (symbol == CHARS) {
			return Constants.CHARS_LIST;
		} else if (symbol == GLOBE) {
			throw new RuntimeException("Can not use globe yet.");
		} else if (symbol == SCOPE) {
			throw new RuntimeException("Can not use scope yet.");
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
