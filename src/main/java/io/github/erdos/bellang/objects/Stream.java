package io.github.erdos.bellang.objects;

public final class Stream implements Expression {

	@Override
	public <T> T apply(ExpressionVisitor<T> visitor) {
		return visitor.stream(this);
	}
}
