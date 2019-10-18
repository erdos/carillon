package io.github.erdos.bellang.objects;

public interface Expression {

	<T> T
	apply(ExpressionVisitor<T> visitor);
}
