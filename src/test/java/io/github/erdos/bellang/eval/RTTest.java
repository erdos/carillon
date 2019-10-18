package io.github.erdos.bellang.eval;

import io.github.erdos.bellang.eval.EvaluationException.WrongArityException;
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

import static io.github.erdos.bellang.eval.RT.eval;
import static io.github.erdos.bellang.eval.RT.list;
import static io.github.erdos.bellang.eval.RT.pair;
import static io.github.erdos.bellang.objects.Character.character;
import static io.github.erdos.bellang.objects.Symbol.NIL;
import static io.github.erdos.bellang.objects.Symbol.symbol;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RTTest {

	@Test
	public void testEvalCharacter() {
		io.github.erdos.bellang.objects.Character c = character('a');
		assertEquals(c, eval(c));
	}

	@Test
	public void testEvalQuoted() {
		Expression expression = list(symbol("a"), symbol("b"));
		assertEquals(expression, eval(RT.quote(expression)));
	}

	@Test
	public void testEvalLit() {
		Expression expression = list(Symbol.LIT, symbol("b"));
		assertEquals(expression, eval(expression));
	}

	@Test
	public void testSymbolsIdentical() throws IOException {
		Expression expression = read("(id 'alabama 'alabama)");
		assertEquals(symbol("t"), eval(expression));
	}

	@Test
	public void testCar() throws IOException {
		assertEquals(symbol("a"), eval(read("(car '(a b))")));
	}

	@Test
	public void testCdr() throws IOException {
		assertEquals(list(symbol("b")), eval(read("(cdr '(a b))")));
	}


	@Test
	public void asdf() throws IOException {
		assertEquals(symbol("x"), eval(read("((fn (a) a)  'x)")));
	}


	@Test
	public void quoteSymbolIsUnbound() {
		assertThrows(EvaluationException.UnboundSymbolException.class,
				() -> eval(read("((fn (f a) (f a)) quote 'x)")));
	}

	@Test
	public void specialSymbolsEvalToThemselves() {
		assertEquals(symbol("t"), eval(symbol("t")));
		assertEquals(symbol("nil"), eval(symbol("nil")));
		assertEquals(symbol("o"), eval(symbol("o")));
		assertEquals(symbol("apply"), eval(symbol("apply")));
	}

	@Test
	public void closure1() throws IOException {
		String input = "((lit clo nil (x) (car x)) '(a b))";
		Expression inputExpression = read(input);
		assertEquals(symbol("a"), eval(inputExpression));
	}

	@Test
	public void join0() {
		assertEquals(pair(NIL, NIL), eval(list(symbol("join"))));
	}

	@Test
	public void join1() throws IOException {
		assertEquals(read("(a b)"), eval(read("(join 'a (join 'b nil))")));
	}

	@Test
	public void if2() throws IOException {
		assertEquals(read("b"), eval(read("(if nil 'a 't 'b)")));
		assertEquals(read("a"), eval(read("(if 't 'a)")));
		assertEquals(read("a"), eval(read("(if 't 'a 'b)")));
		assertEquals(read("b"), eval(read("(if nil 'a 'b)")));

		assertEquals(read("nil"), eval(read("(if nil 'a)")));
		assertEquals(read("nil"), eval(read("(if nil 'a nil 'b)")));
	}

	@Test
	public void testMacUnary() throws IOException {
		eval(read("(mac a (x) (join 'quote (join x nil)))"));
		assertEquals(read("x"), eval(read("(a x)")));
	}


	@Test
	public void testMacOr() throws IOException {
		eval(read("(def no (x) (id x nil))"));
		eval(read("(mac or (a b) (if (no a) b a))"));
		assertEquals(read("b"), eval(read("(or nil 'b)")));
	}

	@Test
	public void testDefUnary() throws IOException {
		eval(read("(def a (x) (join x x))"));
		assertEquals(pair(symbol("x"), symbol("x")), eval(read("(a 'x)")));
	}

	@Test
	public void testNullaryDefinition() throws IOException { // this is the definition of nullary functions!
		eval(read("(def nullary () 'ok)"));
		assertEquals(symbol("ok"), eval(read("(nullary)")));
		assertThrows(WrongArityException.class, () -> eval(read("(nullary 'x)")));
		assertThrows(WrongArityException.class, () -> eval(read("(nullary 'x 'y)")));

	}

	@Test
	public void testCallLessArgs() throws IOException {
		eval(read("(def a (x y z) (join z (join y x)))"));
		assertThrows(WrongArityException.class, () -> eval(read("(a)")));
		assertThrows(WrongArityException.class, () -> eval(read("(a 'f)")));
		assertThrows(WrongArityException.class, () -> eval(read("(a 'f 'g)")));
	}

	@Test
	public void testCallVarargsNone() throws IOException {
		eval(read("(def a xs (car xs))"));
		assertEquals(read("x"), eval(read("(a 'x 'y 'z)")));
		assertEquals(read("nil"), eval(read("(a)")));
	}

	@Test
	public void testCallVarargs() throws IOException {
		eval(read("(def a (x . xs) xs)"));
		assertEquals(read("(two three)"), eval(read("(a 'one 'two 'three)")));
		assertEquals(read("nil"), eval(read("(a 'one)")));
		assertThrows(WrongArityException.class, () -> eval(read("(a)")));
	}

	@Test
	public void testDefBinary() throws IOException {
		eval(read("(def a (x y) (join x y))"));
		assertEquals(read("(x . y)"), eval(read("(a 'x 'y)")));
	}

	@Test
	public void testDefZeroArity() throws IOException {
		eval(read("(def a () 'x)"));
		assertEquals(read("x"), eval(read("(a)")));
	}

	@Test
	public void emptyList() throws IOException {
		assertEquals(read("nil"), read("()"));
	}

	@Ignore
	@Test
	public void testFunctionReturnsFunction() throws IOException {
		eval(read("(def a (x) (join join (join x nil)))")); // a (reduce join ns) teljesen valid scenario!
		System.out.println(eval(read("(a 'f)")));
	}

	// TODO: on calculating optional values - should we also use var bindings from parameters?
	@Test
	public void fnCallWithOptional() throws IOException {
		// System.out.println(RT.eval(read("((fn ((o x 'y)) x))")));

		// last arg is missing so default value is presented.
		assertEquals(symbol("y"), eval(read("((fn (a (o x 'y)) x) 'b)")));

		// last arg is nil so deafult value is not calculated.
		assertEquals(NIL, RT.eval(read("((fn (a (o x 'y)) x) 'b nil)")));

		// last arg is presented and used.
		assertEquals(symbol("c"), eval(read("((fn (a (o x 'y)) x) 'b 'c)")));

	}

	@Test
	public void fnCallMultipleOptionals() throws IOException {
		// no opotional arg is missing
		assertEquals(read("(a b)"), eval(read("((fn ((o x 'X) (o y 'Y)) (join x (join y nil))) 'a 'b)")));
		// last opotional arg is missing
		assertEquals(read("(b Y)"), eval(read("((fn ((o x 'X) (o y 'Y)) (join x (join y nil))) 'b)")));
		// both optional args are missing
		assertEquals(read("(X Y)"), eval(read("((fn ((o x 'X) (o y 'Y)) (join x (join y nil))))")));
	}

	@Test
	public void testClosurePreservesBinding() throws IOException {
		assertEquals(read("x"), eval(read("((let a 'x (fn v a)) 'y)")));
	}

	@Test
	public void testLambdaArg() throws IOException {
		assertEquals(read("y"), eval(read("(let ((nil)) 'x 'y)")));
	}

	@Test
	public void testApply() throws IOException {
		assertEquals(Pair.EMPTY, eval(read("(apply join)")));
		// assertEquals(Pair.EMPTY, RT.eval(read("(apply join '())")));

		// assertEquals(Pair.EMPTY, RT.eval(read("(apply join nil)")));

		Expression result1 = eval(read("(apply join '(a b))"));
		Expression result2 = eval(read("(apply join 'a '(b))"));
		assertEquals(result1, read("(a . b)"));
		assertEquals(result2, read("(a . b)"));
	}

	@Test
	public void testJoin() throws IOException {
		assertEquals(Pair.EMPTY, eval(read("(join)")));
		assertEquals(read("(a)"), eval(read("(join 'a)")));
		assertEquals(read("(a . b)"), eval(read("(join 'a 'b)")));
		assertEquals(pair(NIL, symbol("b")), eval(read("(join nil 'b)")));
		assertEquals(pair(symbol("a"), NIL), eval(read("(join 'a nil)")));
		assertEquals(NIL, eval(read("(id (join 'a 'b) (join 'a 'b))")));
	}

	@Test
	public void testNom() throws IOException {
		assertEquals(read("(\\n \\i \\l)"), eval(read("(nom nil)")));
		assertEquals(read("(\\x \\y \\z)"), eval(read("(nom 'xyz)")));
		assertThrows(WrongArityException.class, () -> eval(read("(nom)")));
		assertThrows(WrongArityException.class, () -> eval(read("(nom 'asdf 'xyze)")));
	}


	public void complexArgs() throws IOException {
		eval(read("(def foo ((o (t (x . y) [caris _ 'a]) '(a . b))) x)"));

		// expecting mistype error
		eval(read("(foo '(b b))"));

		// expecting a
		assertEquals(read("a"), eval(read("(foo)")));
	}

	@Test
	//@Ignore
	public void testBel() {

		try (InputStream stream = RT.class.getResourceAsStream("/bel.bel");
		     InputStreamReader reader = new InputStreamReader(stream);
		     PushbackReader pbr = new PushbackReader(reader)) {

			for(int i = 0; i < 1000; i++) {
				// System.out.println("reading...");
				Expression e = Reader.read(pbr);
				if (e == null) break;
				// System.out.println("    " + i);
				// System.out.println("Evaling " + e);
				Expression out = eval(e);
				// System.out.println("> " + out);
			}

			// System.out.println(eval(read("(tem point x 0 y 0)")));

			complexArgs();
			// itt megfexik
			//System.out.println(eval(read("(literal 'nil)")));

			//System.out.println(eval(read("(literal 'a)")));
			//System.out.println(eval(read("(literal '(nil))")));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Expression read(String s) throws IOException {
		return new Reader().read(new PushbackReader(new StringReader(s)));
	}
}