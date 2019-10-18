package io.github.erdos.carillon.objects;

public interface Expression {

	<T> T
	apply(ExpressionVisitor<T> visitor);
}
