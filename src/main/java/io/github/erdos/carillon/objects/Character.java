package io.github.erdos.carillon.objects;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Character implements Expression {
	private final char c;

	private Character(char c) {this.c = c;}

	private static final Map<java.lang.Character, Character> pool = new ConcurrentHashMap<>();

	public static Character character(char c) {
		return pool.computeIfAbsent(c, Character::new);
	}

	@Override
	public String toString() {
		return "\\" + c;
	}

	public char getChar() {
		return c;
	}

	@Override
	public <T> T apply(ExpressionVisitor<T> visitor) {
		return visitor.character(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Character character = (Character) o;
		return c == character.c;
	}

	@Override
	public int hashCode() {
		return c;
	}
}
