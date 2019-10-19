package io.github.erdos.carillon.eval;

import io.github.erdos.carillon.objects.Expression;
import io.github.erdos.carillon.objects.Symbol;
import io.github.erdos.carillon.reader.Reader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BelTest {

	@org.junit.jupiter.api.BeforeAll
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
	public void testSetLocation() {
		assertEquals(read("(a z c)"), eval("(let x '(a b c) (set (cadr x) 'z) x)"));
	}

	private static Expression eval(String s) {
		return RT.eval(read(s));
	}

	private static Expression read(String s) {
		try {
			return new Reader().read(new PushbackReader(new StringReader(s)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}