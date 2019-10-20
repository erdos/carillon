package io.github.erdos.carillon.eval;

import io.github.erdos.carillon.objects.Expression;
import io.github.erdos.carillon.objects.Symbol;
import io.github.erdos.carillon.reader.Reader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("manual")
class BelTest {

	@BeforeAll
	public static void setup() {

		try (InputStream stream = RT.class.getResourceAsStream("/bel.bel");
		     InputStreamReader reader = new InputStreamReader(stream);
		     PushbackReader pbr = new PushbackReader(reader)) {

			for (Expression e = Symbol.NIL; e != null; e = Reader.read(pbr)) {
				RT.eval(e);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testZap() {
		assertEquals(read("(b c)"), eval("(let x '(a b c) (zap cdr x) x)"));
	}

	@Test
	public void testPushPull() {
		assertEquals(read("(z a b)"), eval("(let x '(a b c) (push 'z x) (pull 'c x) x)"));
	}

	@Test
	public void testPop() {
		assertEquals(read("(a (b c))"), eval("(let x '(a b c) (list (pop x) x))"));
		assertEquals(read("(a c)"), eval("(let x '(a b c)  (pop (cdr x)) x)"));
	}

	@Test
	public void testSetLocation() {
		assertEquals(read("(a z c)"), eval("(let x '(a b c) (set (cadr x) 'z) x)"));
	}

	@Test
	public void testSet() {
		assertEquals(read("((a))"), eval("(set x '((a)) y x)"));
		assertEquals(read("((a))"), eval("y"));
		assertEquals(read("(hello a)"), eval("(push 'hello (car x))"));
		assertEquals(read("((hello a))"), eval("y"));
	}

	@Test
	public void testDrop() {
		assertEquals(read("(c d e)"), eval("(drop 2 '(a b c d e))"));
	}

	@Test
	public void testFirst() {
		assertEquals(read("(a b)"), eval("(first 2 '(a b c d e))"));
	}

	@Test
	public void testNth() {
		assertEquals(read("b"), eval("(nth 2 '(a b c d e))"));
	}

	@Test
	public void testNchar() {
		assertEquals(read("\\A"), eval("(nchar 65)"));
	}

	@Test
	public void testCut() {
		// definition of cut is tricky: one optional parameter has a default value based on an other parameter.
		assertEquals(read("(\\o \\o \\b)"), eval("(cut \"foobar\" 2 4)"));
		// TODO: also (cut "foobar" 2 -1) -> "ooba"
	}

	@Test
	public void testExampleGoodness() {
		assertEquals(read("10"), eval("(let m (macro (x) (sym (append (nom x) \"ness\"))) (set (m good) 10))"));
		assertEquals(read("10"), eval("goodness"));
	}

	@Test
	public void testDedup() {
		assertEquals(eval("\"\""), eval("(dedup \"\")"));
		assertEquals(eval("\"abc\""), eval("(dedup \"aabccc\")"));
	}

	@Test
	@Disabled
	public void testDedupSort() {
		// assertEquals(read("acd"), eval("(dedup:sort < \"acdc\")"));
		assertEquals(eval("\"abc\""), eval("(dedup \"aabccc\")"));

		//assertEquals(read("acd"), eval("(dedup:sort < \"ab\")"));

	}

	// TODO: most ez a legfontosabb!
	@Test
	@Disabled
	public void testCharn() {
		assertEquals(read("64"), eval("(charn \\a)"));
	}

	@Disabled
	@Test
	public void testCompare() {
		assertEquals(Symbol.T, eval("(< \\a \\b)"));
		assertEquals(Symbol.NIL, eval("(< \\b \\b)"));
		assertEquals(Symbol.NIL, eval("(< \\c \\b)"));
	}

	private static Expression eval(String s) {
		return RT.eval(read(s));
	}

	private static Expression read(String s) {
		try {
			new Reader();
			return Reader.read(new PushbackReader(new StringReader(s)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}