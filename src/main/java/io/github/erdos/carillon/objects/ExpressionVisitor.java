package io.github.erdos.carillon.objects;

public interface ExpressionVisitor<T> {

	T pair(Pair pair);

	T stream(Stream stream);

	T symbol(Symbol symbol);

	T character(Character character);
}
