package io.github.erdos.bellang.eval;

import io.github.erdos.bellang.objects.Expression;
import io.github.erdos.bellang.objects.Pair;
import io.github.erdos.bellang.objects.Symbol;
import io.github.erdos.bellang.reader.Reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.util.List;
import java.util.Stack;

import static io.github.erdos.bellang.objects.Symbol.CLO;
import static io.github.erdos.bellang.objects.Symbol.LIT;
import static io.github.erdos.bellang.objects.Symbol.NIL;
import static io.github.erdos.bellang.objects.Symbol.QUOTE;

public class RT {

	private static final ExpressionEvaluatorVisitor visitor = new ExpressionEvaluatorVisitor();

	static {
		try (InputStream stream = RT.class.getResourceAsStream("/prelude.bel");
		     InputStreamReader reader = new InputStreamReader(stream);
		     PushbackReader pbr = new PushbackReader(reader)) {
			Expression e;

			eval(Reader.read(pbr));

/*			while ((e = Reader.read(pbr)) != null) {
				System.out.println("Evaluating " + e);
				eval(e);
			}*/
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
		Stack<Expression> es = new Stack<>();
		coll.forEach(es::push);

		Pair p = null;
		for (Expression e : es) {
			p = pair(e, p);
		}
		return p;
	}

	// (def n p e) -> (set n (lit clo nil p e))

	// (mac n p e) -> (set n (lit mac (lit clo nil p e)))

	public static Pair fn(List<Symbol> args, Expression body) {
		return list(LIT, CLO, null, list(args), body);
	}

	public static Pair quote(Expression body) {
		return list(QUOTE, body);
	}

	public static Pair lit(Expression body) {
		return list(LIT, body);
	}
}
