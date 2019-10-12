package io.github.erdos.bellang.eval;

import io.github.erdos.bellang.objects.Expression;
import io.github.erdos.bellang.objects.Pair;
import io.github.erdos.bellang.objects.Symbol;
import io.github.erdos.bellang.reader.Reader;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.StringReader;

import static io.github.erdos.bellang.eval.RT.list;
import static io.github.erdos.bellang.eval.RT.pair;
import static io.github.erdos.bellang.objects.Character.character;
import static io.github.erdos.bellang.objects.Symbol.NIL;
import static io.github.erdos.bellang.objects.Symbol.symbol;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RTTest {

	@Test
	public void testEvalCharacter() {
		io.github.erdos.bellang.objects.Character c = character('a');
		assertEquals(c, RT.eval(c));
	}

	@Test
	public void testEvalQuoted() {
		Expression expression = list(symbol("a"), symbol("b"));
		assertEquals(expression, RT.eval(RT.quote(expression)));
	}

	@Test
	public void testEvalLit() {
		Expression expression = list(Symbol.LIT, symbol("b"));
		assertEquals(expression, RT.eval(expression));
	}

	@Test
	public void testSymbolsIdentical() throws IOException {
		Expression expression = read("(id 'alabama 'alabama)");
		assertEquals(symbol("t"), RT.eval(expression));
	}

	@Test
	public void testCar() throws IOException {
		assertEquals(symbol("a"), RT.eval(read("(car '(a b))")));
	}

	@Test
	public void testCdr() throws IOException {
		Expression expression = read("(cdr '(a b))");
		assertEquals(list(symbol("b")), RT.eval(expression));
	}

	@Test
	public void specialSymbolsEvalToThemselves() {
		assertEquals(symbol("t"), RT.eval(symbol("t")));
		assertEquals(symbol("nil"), RT.eval(symbol("nil")));
		assertEquals(symbol("o"), RT.eval(symbol("o")));
		assertEquals(symbol("apply"), RT.eval(symbol("apply")));
	}

	@Test
	public void closure1() throws IOException {
		String input = "((lit clo nil (x) (car x)) '(a b))";
		Expression inputExpression = read(input);
		assertEquals(symbol("a"), RT.eval(inputExpression));
	}

	@Test
	public void join0() {
		assertEquals(pair(NIL, NIL), RT.eval(list(symbol("join"))));
	}

	@Test
	public void join1() throws IOException {
		assertEquals(read("(a b)"), RT.eval(read("(join 'a (join 'b nil))")));
	}

	@Test
	public void if2() throws IOException {
		assertEquals(read("b"), RT.eval(read("(if nil 'a 't 'b)")));
		assertEquals(read("a"), RT.eval(read("(if 't 'a)")));
		assertEquals(read("a"), RT.eval(read("(if 't 'a 'b)")));
		assertEquals(read("b"), RT.eval(read("(if nil 'a 'b)")));

		assertEquals(read("nil"), RT.eval(read("(if nil 'a)")));
		assertEquals(read("nil"), RT.eval(read("(if nil 'a nil 'b)")));
	}

	@Test
	public void testDef() throws IOException {
		RT.eval(read("(def a (x) (join x x))"));
		assertEquals(pair(symbol("x"), symbol("x")), RT.eval(read("(a 'x)")));
	}

	@Test
	public void testDef2() throws IOException {
		RT.eval(read("(def a (x) (x 'b 'c))")); // a (reduce join ns) teljesen valid scenario!
		System.out.println(RT.eval(read("(a join)")));
	}

	@Test
	public void testApply() throws IOException {
		assertEquals(Pair.EMPTY, RT.eval(read("(apply join)")));
		// assertEquals(Pair.EMPTY, RT.eval(read("(apply join '())")));

		// assertEquals(Pair.EMPTY, RT.eval(read("(apply join nil)")));

		Expression result1 = RT.eval(read("(apply join '(a b))"));
		Expression result2 = RT.eval(read("(apply join 'a '(b))"));
		assertEquals(result1, read("(a . b)"));
		assertEquals(result2, read("(a . b)"));
	}

	@Test
	@Ignore
	public void testBel() {

		try (InputStream stream = RT.class.getResourceAsStream("/bel.bel");
		     InputStreamReader reader = new InputStreamReader(stream);
		     PushbackReader pbr = new PushbackReader(reader)) {

			for(int i = 0; i < 30; i++) {
				Expression e = Reader.read(pbr);
				System.out.println("    " + i);
				System.out.println("Evaling " + e);
				Expression out = RT.eval(e);
				System.out.println("> " + out);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Expression read(String s) throws IOException {
		return new Reader().read(new PushbackReader(new StringReader(s)));
	}
}