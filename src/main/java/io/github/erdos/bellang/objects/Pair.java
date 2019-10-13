package io.github.erdos.bellang.objects;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import static io.github.erdos.bellang.objects.Symbol.NIL;

public final class Pair implements Expression, Iterable<Expression> {

	public static final Pair EMPTY = new Pair(NIL, NIL);

	private Expression first;
	private Expression second;

	public Pair(Expression first, Expression second) {
		assert second != null;
		this.first = first;
		this.second = second;
	}

	@Override
	public String toString() {
		if (first == Symbol.QUOTE && second instanceof Pair && (((Pair) second).second == NIL || ((Pair) second).second == null)) {
			return "'" + ((Pair) second).first;
		}
		StringBuilder sb = new StringBuilder("(");
		if (first == null) {
			sb.append("nil");
		} else {
			sb.append(first.toString());
		}

		Pair current = this;

		while (current.second instanceof Pair && !current.isRightNil()) {
			current = (Pair) (current.second);

			sb.append(' ');

			if (current.first == null) {
				sb.append("nil");
			} else {
				sb.append(current.first);
			}
		}

		if (!current.isRightNil()) {
			sb.append(' ');
			sb.append('.');
			sb.append(' ');
			sb.append(current.second == null ? "nil" : current.second.toString());
		}

		sb.append(')');
		return sb.toString();
	}

	@Override
	public <T> T apply(ExpressionVisitor<T> visitor) {
		return visitor.pair(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Pair pair = (Pair) o;
		return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}

	@Override
	public Iterator<Expression> iterator() {
		return new Iterator<Expression>() {
			Pair head = Pair.this;

			@Override
			public boolean hasNext() {
				return head != null;
			}

			@Override
			public Expression next() {
				if (head == null) {
					throw new NoSuchElementException();
				}
				Expression result = head.first;

				if (head.second == NIL) {
					head = null;
				} else {
					head = (Pair) (head.second);
				}
				return result;
			}
		};
	}

	public Expression car() {
		return first;
	}

	public Expression cdr() {
		return second;
	}

	// second elem in normal list
	public Expression cadr() {
		return ((Pair) second).first;
	}

	// third elem
	public Expression caddr() {
		return next().next().first;
	}

	public Pair next() {
		return ((Pair) second);
	}

	public Expression cadddr() {
		return ((Pair) ((Pair) ((Pair) second).second).second).first;
	}

	public Expression caddddr() {
		return ((Pair) ((Pair) ((Pair) ((Pair) second).second).second).second).first;
	}

	public void setCar(Expression e) {
		this.first = e;
	}

	public void setCdr(Expression e) {
		this.second = e;
	}

	public boolean isEmpty() {
		return first == NIL && second == NIL;
	}

	public boolean isRightNil() {
		return second == NIL || second == null;
	}

	public Expression nth(int n) {
		Pair p = this;
		for (int i = 0; i < n; i++) {
			p = (Pair) p.second;
		}
		return p.car();
	}

	public Expression nthOrNil(int n) {
		Pair p = this;
		for (int i = 0; i < n; i++) {
			if (p.second == NIL) {
				return NIL;
			} else {
				p = (Pair) p.second;
			}
		}
		return p.car();
	}
}
