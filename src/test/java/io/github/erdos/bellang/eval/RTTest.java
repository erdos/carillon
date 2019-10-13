package io.github.erdos.bellang.eval;

import io.github.erdos.bellang.objects.Expression;
import io.github.erdos.bellang.objects.Pair;
import io.github.erdos.bellang.objects.Symbol;
import io.github.erdos.bellang.reader.Reader;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
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
	public void asdf() throws IOException {
		assertEquals(symbol("x"), RT.eval(read("((fn (a) a)  'x)")));
	}


	@Test
	public void dfdf() throws IOException {
		//  ????
		assertEquals(symbol("x"), RT.eval(read("((fn (f a) (f a)) quote 'x)")));
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
	public void testMacUnary() throws IOException {
		RT.eval(read("(mac a (x) (join 'quote (join x nil)))"));
		assertEquals(read("x"), RT.eval(read("(a x)")));
	}


	@Test
	public void testDefUnary() throws IOException {
		RT.eval(read("(def a (x) (join x x))"));
		assertEquals(pair(symbol("x"), symbol("x")), RT.eval(read("(a 'x)")));
	}

	@Test
	public void testCallLessArgs() throws IOException {
		RT.eval(read("(def a (x y z) (join z (join y x)))"));
		Assertions.assertThrows(EvaluationException.class, () -> RT.eval(read("(a)")));
		Assertions.assertThrows(EvaluationException.class, () -> RT.eval(read("(a 'f)")));
		Assertions.assertThrows(EvaluationException.class, () -> RT.eval(read("(a 'f 'g)")));
	}

	@Test
	public void testCallVarargsNone() throws IOException {
		RT.eval(read("(def a xs (car xs))"));
		assertEquals(read("x"), RT.eval(read("(a 'x 'y 'z)")));
		assertEquals(read("nil"), RT.eval(read("(a)")));
	}

	@Test
	public void testCallVarargs() throws IOException {
		RT.eval(read("(def a (x . xs) xs)"));
		assertEquals(read("(two three)"), RT.eval(read("(a 'one 'two 'three)")));
		assertEquals(read("nil"), RT.eval(read("(a 'one)")));
		assertEquals(read("nil"), RT.eval(read("(a)")));
	}

	@Test
	public void testDefBinary() throws IOException {
		RT.eval(read("(def a (x y) (join x y))"));
		assertEquals(read("(x . y)"), RT.eval(read("(a 'x 'y)")));
	}

	@Ignore
	@Test
	public void testFunctionReturnsFunction() throws IOException {
		RT.eval(read("(def a (x) (join join (join x nil)))")); // a (reduce join ns) teljesen valid scenario!
		System.out.println(RT.eval(read("(a 'f)")));
	}


	// TODO: on calculating optional values - should we also use var bindings from parameters?
	@Test
	public void fnCallWithOptional() throws IOException {
		// System.out.println(RT.eval(read("((fn ((o x 'y)) x))")));

		// last arg is missing so default value is presented.
		assertEquals(symbol("y"), RT.eval(read("((fn (a (o x 'y)) x) 'b)")));

		// last arg is nil so deafult value is not calculated.
		// assertEquals(NIL, RT.eval(read("((fn (a (o x 'y)) x) 'b nil)")));

		// last arg is presented and used.
		assertEquals(symbol("c"), RT.eval(read("((fn (a (o x 'y)) x) 'b 'c)")));

	}

	@Test
	public void testClosurePreservesBinding() throws IOException {
		assertEquals(read("x"), RT.eval(read("((let a 'x (fn v a)) 'y)")));
	}

	@Test
	public void defLast() throws IOException {
		Expression read = read("(def last (xs) (if (id (cdr xs) nil) (car xs) (last (cdr xs))        )   )");
		System.out.println("> " + read);
		System.out.println(RT.eval(read));
		System.out.println(RT.eval(read("(last '(a b c d))")));

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
	//@Ignore
	public void testBel() {

		try (InputStream stream = RT.class.getResourceAsStream("/bel.bel");
		     InputStreamReader reader = new InputStreamReader(stream);
		     PushbackReader pbr = new PushbackReader(reader)) {

			for(int i = 0; i < 100; i++) {
				System.out.println("reading...");
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