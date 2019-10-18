package io.github.erdos.carillon.eval;

import io.github.erdos.carillon.objects.Expression;
import io.github.erdos.carillon.objects.Symbol;

import java.util.function.Supplier;

public class EvaluationException extends RuntimeException {

	public EvaluationException(Expression expression, String msg) {
		super(msg + " expression=" + expression);
	}

	public static Supplier<EvaluationException> evalException(Expression e, String msg) {
		return () -> new EvaluationException(e, msg);
	}

	public static final class WrongArityException extends EvaluationException {

		public WrongArityException(Expression arguments, Expression values) {
			super(arguments, "Function called with wrong arity!");
		}
	}

	public static final class UnboundSymbolException extends EvaluationException {
		public UnboundSymbolException(Symbol symbol) {
			super(symbol, "Unbound symbol!");
		}
	}

	public static final class FeatureNotImplementedException extends EvaluationException {
		public FeatureNotImplementedException(Expression expression) {
			super(expression, "This feature is not implemented!");
		}
	}
}
