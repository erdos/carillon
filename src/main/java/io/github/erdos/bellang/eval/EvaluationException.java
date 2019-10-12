package io.github.erdos.bellang.eval;

import io.github.erdos.bellang.objects.Expression;

public class EvaluationException extends RuntimeException {

	public EvaluationException(Expression expression, String msg) {
		super(msg);
	}
}
