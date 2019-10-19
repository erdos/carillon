package io.github.erdos.carillon.eval;

import io.github.erdos.carillon.eval.EvaluationException.WrongArityException;
import io.github.erdos.carillon.objects.Expression;
import io.github.erdos.carillon.objects.Pair;
import io.github.erdos.carillon.objects.Symbol;
import io.github.erdos.carillon.reader.Reader;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import static io.github.erdos.carillon.eval.RT.list;
import static io.github.erdos.carillon.eval.RT.pair;
import static io.github.erdos.carillon.objects.Character.character;
import static io.github.erdos.carillon.objects.Symbol.NIL;
import static io.github.erdos.carillon.objects.Symbol.symbol;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RTTest {

	@Test
	public void testEvalCharacter() {
		io.github.erdos.carillon.objects.Character c = character('a');
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
	public void testSymbolsIdentical() {
		Expression expression = read("(id 'alabama 'alabama)");
		assertEquals(symbol("t"), eval(expression));
	}

	@Test
	public void testCar() {
		assertEquals(symbol("a"), eval(read("(car '(a b))")));
	}

	@Test
	public void testCdr() {
		assertEquals(list(symbol("b")), eval(read("(cdr '(a b))")));
	}


	@Test
	public void asdf() {
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
	public void closure1() {
		String input = "((lit clo nil (x) (car x)) '(a b))";
		Expression inputExpression = read(input);
		assertEquals(symbol("a"), eval(inputExpression));
	}

	@Test
	public void join0() {
		assertEquals(pair(NIL, NIL), eval(list(symbol("join"))));
	}

	@Test
	public void join1() {
		assertEquals(read("(a b)"), eval(read("(join 'a (join 'b nil))")));
	}

	@Test
	public void if2() {
		assertEquals(read("b"), eval(read("(if nil 'a 't 'b)")));
		assertEquals(read("a"), eval(read("(if 't 'a)")));
		assertEquals(read("a"), eval(read("(if 't 'a 'b)")));
		assertEquals(read("b"), eval(read("(if nil 'a 'b)")));

		assertEquals(read("nil"), eval(read("(if nil 'a)")));
		assertEquals(read("nil"), eval(read("(if nil 'a nil 'b)")));
	}

	@Test
	public void testMacUnary() {
		eval(read("(mac a (x) (join 'quote (join x nil)))"));
		assertEquals(read("x"), eval(read("(a x)")));
	}


	@Test
	public void testMacOr() {
		eval(read("(def no (x) (id x nil))"));
		eval(read("(mac or (a b) (if (no a) b a))"));
		assertEquals(read("b"), eval(read("(or nil 'b)")));
	}

	@Test
	public void testDefUnary() {
		eval(read("(def a (x) (join x x))"));
		assertEquals(pair(symbol("x"), symbol("x")), eval(read("(a 'x)")));
	}

	@Test
	public void testNullaryDefinition() { // this is the definition of nullary functions!
		eval(read("(def nullary () 'ok)"));
		assertEquals(symbol("ok"), eval(read("(nullary)")));
		assertThrows(WrongArityException.class, () -> eval(read("(nullary 'x)")));
		assertThrows(WrongArityException.class, () -> eval(read("(nullary 'x 'y)")));

	}

	@Test
	public void testCallLessArgs() {
		eval(read("(def a (x y z) (join z (join y x)))"));
		assertThrows(WrongArityException.class, () -> eval(read("(a)")));
		assertThrows(WrongArityException.class, () -> eval(read("(a 'f)")));
		assertThrows(WrongArityException.class, () -> eval(read("(a 'f 'g)")));
	}

	@Test
	public void testCallVarargsNone() {
		eval(read("(def a xs (car xs))"));
		assertEquals(read("x"), eval(read("(a 'x 'y 'z)")));
		assertEquals(read("nil"), eval(read("(a)")));
	}

	@Test
	public void testCallVarargs() {
		eval(read("(def a (x . xs) xs)"));
		assertEquals(read("(two three)"), eval(read("(a 'one 'two 'three)")));
		assertEquals(read("nil"), eval(read("(a 'one)")));
		assertThrows(WrongArityException.class, () -> eval(read("(a)")));
	}

	@Test
	public void testDefBinary() {
		eval(read("(def a (x y) (join x y))"));
		assertEquals(read("(x . y)"), eval(read("(a 'x 'y)")));
	}

	@Test
	public void testDefZeroArity() {
		eval(read("(def a () 'x)"));
		assertEquals(read("x"), eval(read("(a)")));
	}

	@Test
	public void emptyList() {
		assertEquals(read("nil"), read("()"));
	}

	@Ignore
	@Test
	public void testFunctionReturnsFunction() {
		eval(read("(def a (x) (join join (join x nil)))")); // a (reduce join ns) teljesen valid scenario!
		System.out.println(eval(read("(a 'f)")));
	}

	// TODO: on calculating optional values - should we also use var bindings from parameters?
	@Test
	public void fnCallWithOptional() {
		// System.out.println(RT.eval(read("((fn ((o x 'y)) x))")));

		// last arg is missing so default value is presented.
		assertEquals(symbol("y"), eval(read("((fn (a (o x 'y)) x) 'b)")));

		// last arg is nil so deafult value is not calculated.
		assertEquals(NIL, RT.eval(read("((fn (a (o x 'y)) x) 'b nil)")));

		// last arg is presented and used.
		assertEquals(symbol("c"), eval(read("((fn (a (o x 'y)) x) 'b 'c)")));

	}

	@Test
	public void fnCallMultipleOptionals() {
		// no opotional arg is missing
		assertEquals(read("(a b)"), eval(read("((fn ((o x 'X) (o y 'Y)) (join x (join y nil))) 'a 'b)")));
		// last opotional arg is missing
		assertEquals(read("(b Y)"), eval(read("((fn ((o x 'X) (o y 'Y)) (join x (join y nil))) 'b)")));
		// both optional args are missing
		assertEquals(read("(X Y)"), eval(read("((fn ((o x 'X) (o y 'Y)) (join x (join y nil))))")));
	}

	@Test
	public void testClosurePreservesBinding() {
		assertEquals(read("x"), eval(read("((let a 'x (fn v a)) 'y)")));
	}

	@Test
	public void testLambdaArg() {
		assertEquals(read("y"), eval(read("(let ((nil)) 'x 'y)")));
	}

	@Test
	public void testApply() {
		assertEquals(Pair.EMPTY, eval(read("(apply join)")));
		// assertEquals(Pair.EMPTY, RT.eval(read("(apply join '())")));

		// assertEquals(Pair.EMPTY, RT.eval(read("(apply join nil)")));

		Expression result1 = eval(read("(apply join '(a b))"));
		Expression result2 = eval(read("(apply join 'a '(b))"));
		assertEquals(result1, read("(a . b)"));
		assertEquals(result2, read("(a . b)"));
	}

	@Test
	public void testJoin() {
		assertEquals(Pair.EMPTY, eval(read("(join)")));
		assertEquals(read("(a)"), eval(read("(join 'a)")));
		assertEquals(read("(a . b)"), eval(read("(join 'a 'b)")));
		assertEquals(pair(NIL, symbol("b")), eval(read("(join nil 'b)")));
		assertEquals(pair(symbol("a"), NIL), eval(read("(join 'a nil)")));
		assertEquals(NIL, eval(read("(id (join 'a 'b) (join 'a 'b))")));
	}

	@Test
	public void testNom() {
		assertEquals(read("(\\n \\i \\l)"), eval(read("(nom nil)")));
		assertEquals(read("(\\x \\y \\z)"), eval(read("(nom 'xyz)")));
		assertThrows(WrongArityException.class, () -> eval(read("(nom)")));
		assertThrows(WrongArityException.class, () -> eval(read("(nom 'asdf 'xyze)")));
	}


	@Test
	public void complexArgs() {
		eval(read("(def foo ((o (t (x . y) [caris _ 'a]) '(a . b))) x)"));

		// expecting mistype error
		eval(read("(foo '(b b))"));

		// expecting a
		assertEquals(read("a"), eval(read("(foo)")));
	}

	@Test
	public void testSetGlobals() {
		assertEquals(read("(a b c)"), eval(read("(set x '(a b c) y x)")));
		assertEquals(read("z"), eval(read("(set (cadr x) 'z)")));
		assertEquals(read("(a z c)"), eval(read("y")));
	}

	@Test
	public void testWhere1() {
		assertEquals(read("((x . a) d)"), eval(read(" (let x 'a (where x))")));
	}

	@Test
	public void testWhere2() {
		eval(read("(set x '(a b c))"));
		assertEquals(read("((b c) a)"), eval(read("(where (cadr x))")));
	}

	@Test
	public void testDyn() {
		eval(read("(set x 'a)"));
		assertEquals(read("(z . b)"), eval(read("(dyn x 'z (join x 'b))")));
		assertEquals(read("a"), eval(read("x")));
	}

	private static Expression eval(String s) {
		return RT.eval(read(s));
	}

	private static Expression eval(Expression e) {
		return RT.eval(e);
	}

	private static Expression read(String s) {
		try {
			return new Reader().read(new PushbackReader(new StringReader(s)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}