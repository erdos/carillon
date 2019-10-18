package io.github.erdos.carillon.eval;

import io.github.erdos.carillon.objects.Expression;
import io.github.erdos.carillon.objects.Pair;
import io.github.erdos.carillon.objects.Symbol;

import java.util.Deque;
import java.util.LinkedList;

public class SpecialForms {
	public Expression evalIf(Pair cond, ExpressionEvaluatorVisitor visitor) {
		assert Symbol.IF.equals(cond.car());

		cond = (Pair) cond.cdr();

		while (cond != null) {
			Expression evaled = visitor.appliedTo(cond.car());

			if (evaled != Symbol.NIL) {
				return visitor.appliedTo(cond.cadr());
			} else if (((Pair) cond.cdr()).cdr() == Symbol.NIL) {
				break;
			} else if (   ((Pair)((Pair) cond.cdr()).cdr()).cdr() == Symbol.NIL) {
				return visitor.appliedTo(cond.caddr());
			} else {
				cond = (Pair) ((Pair) cond.cdr()).cdr();
			}
		}

		return Symbol.NIL;
	}

	public Expression evalQuote(Pair quoted) {
		assert Symbol.QUOTE.equals(quoted.car());
		return quoted.cadr();
	}

	public Expression evalLit(Pair lit) {
		assert Symbol.LIT.equals(lit.car());
		return lit;
	}

	public Expression evalApply(Pair lit, ExpressionEvaluatorVisitor visitor) {
		assert Symbol.APPLY.equals(lit.car());

		Deque<Expression> stack = new LinkedList<>();
		((Pair)lit.cdr()).forEach(stack::push);

		if (stack.size() > 1) {
			((Pair) stack.pop().apply(visitor)).forEach(x -> stack.push(RT.quote(x)));
		}

		Expression last = Symbol.NIL;

		for(Expression e : stack) {
			last = RT.pair(e, last);
		}

		return visitor.appliedTo(last);
	}

	// where -- wtf
	// dyn -- dynamic binding
	// after -- sorrendiseg megtartasa
	// ccc -- wtf
	// thread -- wtf

}
