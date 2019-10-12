package io.github.erdos.bellang.eval;

import io.github.erdos.bellang.objects.Expression;
import io.github.erdos.bellang.objects.Pair;
import io.github.erdos.bellang.objects.Symbol;

public class SpecialForms {
	public Expression evalIf(Pair cond, ExpressionEvaluatorVisitor visitor) {
		assert Symbol.IF.equals(cond.car());
		return null;
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
		throw new RuntimeException("Not implemented!");
	}

	// where -- wtf
	// dyn -- dynamic binding
	// after -- sorrendiseg megtartasa
	// ccc -- wtf
	// thread -- wtf

}
