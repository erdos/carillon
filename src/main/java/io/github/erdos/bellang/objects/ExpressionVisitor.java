package io.github.erdos.bellang.objects;

public interface ExpressionVisitor<T> {

	T pair(Pair pair);

	T stream(Stream stream);

	T symbol(Symbol symbol);

	T character(Character character);
}
