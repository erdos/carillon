package io.github.erdos.bellang.eval;

import io.github.erdos.bellang.objects.Expression;
import io.github.erdos.bellang.objects.Symbol;
import io.github.erdos.bellang.reader.Reader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import static io.github.erdos.bellang.eval.RT.eval;
import static io.github.erdos.bellang.objects.Symbol.symbol;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PreludeTest {


	public static final Symbol A = symbol("a");
	public static final Symbol B = symbol("b");


	@Test
	public void testLet() throws IOException {
		assertEquals(A, eval(read("(let x 'a (let y 'b x))")));
		assertEquals(B, eval(read("(let x 'a (let x 'b x))")));
	}

	private static Expression read(String s) {
		try {
			return new Reader().read(new PushbackReader(new StringReader(s)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}