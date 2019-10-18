package io.github.erdos.carillon.eval;

import io.github.erdos.carillon.objects.Expression;
import io.github.erdos.carillon.objects.Pair;
import io.github.erdos.carillon.reader.Reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.util.Deque;
import java.util.LinkedList;

import static io.github.erdos.carillon.objects.Symbol.NIL;
import static io.github.erdos.carillon.objects.Symbol.QUOTE;

public class RT {

	private static final ExpressionEvaluatorVisitor visitor = new ExpressionEvaluatorVisitor();


	static {
		try (InputStream stream = RT.class.getResourceAsStream("/prelude.bel");
		     InputStreamReader reader = new InputStreamReader(stream);
		     PushbackReader pbr = new PushbackReader(reader)) {

			for (Expression e; (e = Reader.read(pbr)) != null; eval(e)) ;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Expression eval(Expression expression) {
		return visitor.appliedTo(expression);
	}

	public static Pair pair(Expression a, Expression b) {
		return new Pair(a, b);
	}

	public static Pair list(Expression a) {
		return pair(a, NIL);
	}

	public static Pair list(Expression a, Expression b) {
		return pair(a, pair(b, NIL));
	}

	public static Pair list(Expression a, Expression b, Expression c) {
		return pair(a, pair(b, pair(c, NIL)));
	}

	public static Pair list(Expression a, Expression b, Expression c, Expression d) {
		return pair(a, pair(b, pair(c, pair(d, NIL))));
	}

	public static Pair list(Expression a, Expression b, Expression c, Expression d, Expression e) {
		return pair(a, pair(b, pair(c, pair(d, pair(e, NIL)))));
	}

	public static Pair list(Iterable<? extends Expression> coll) {

		assert coll.iterator().hasNext();

		Deque<Expression> es = new LinkedList<>();
		coll.forEach(es::push);

		Expression p = NIL;
		for (Expression e : es) {
			p = pair(e, p);
		}
		return (Pair) p;
	}

	public static Pair quote(Expression body) {
		return list(QUOTE, body);
	}
}
