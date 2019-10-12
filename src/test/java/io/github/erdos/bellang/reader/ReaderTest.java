package io.github.erdos.bellang.reader;

import io.github.erdos.bellang.objects.Character;
import io.github.erdos.bellang.objects.Expression;
import io.github.erdos.bellang.objects.Pair;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import static io.github.erdos.bellang.eval.RT.list;
import static io.github.erdos.bellang.eval.RT.pair;
import static io.github.erdos.bellang.eval.RT.quote;
import static io.github.erdos.bellang.objects.Character.character;
import static io.github.erdos.bellang.objects.Symbol.NIL;
import static io.github.erdos.bellang.objects.Symbol.symbol;
import static org.junit.jupiter.api.Assertions.*;

class ReaderTest {

	@Test
	public void testReadCharacter() throws IOException {
		Character result = new Reader().readCharacter(new PushbackReader(new StringReader("\\a")));
		Character expected = new Character('a');
		assertEquals(expected, result);
	}

	@Test
	public void testReadPairEmpty() throws IOException {
		assertEquals(Pair.EMPTY, read("()"));
	}

	@Test
	public void testReadPairOne() throws IOException {
		Pair result = new Reader().readPair(new PushbackReader(new StringReader("(\\x)")));
		Pair expected = pair(new Character('x'), NIL);
		assertEquals(expected, result);
	}

	@Test
	public void testPairTwo() throws IOException {
		Pair result = new Reader().readPair(new PushbackReader(new StringReader("(\\x \\y)")));
		Pair expected = pair(character('x'), pair(character('y'), NIL));
		assertEquals(expected, result);
	}

	@Test
	public void testPairTwoDot() throws IOException {
		Expression result = read("(\\x . \\y)");
		Pair expected = pair( character('x'), character('y'));
		assertEquals(expected, result);
	}

	@Test
	public void testFnShorthand() throws IOException {
		Expression result = read("[f _ x]");
		Expression result2 = read("(fn (_) (f _ x))");
		assertEquals(result, result2);
	}

	@Test
	public void readQuotedShorthand() throws IOException {
		Expression expected = quote(symbol("alabama"));

		Expression result1 = read("'alabama");
		assertEquals(expected, result1);

		Expression result2 = read("(quote alabama)");
		assertEquals(expected, result2);
	}

	@Test
	public void readTypeExpression() throws IOException {
		assertEquals(list(symbol("t"), symbol("x"), symbol("pair")), read("x|pair"));
	}

	@Test
	public void readSymbolSplit() throws IOException {
		assertEquals(list(symbol("a"), symbol("b")), read("a.b"));
	}

	@Test
	public void readSymbolQuote() throws IOException {
		assertEquals(list(symbol("a"), quote(symbol("b"))), read("a!b"));
	}

	@Test
	public void testPairThree() throws IOException {
		Expression result = read("(\\x \\y \\z)");
		Pair expected = list( character('x'), character('y'), character('z'));
		assertEquals(expected, result);
	}

	@Test
	public void testReadBackticket1() throws IOException {
		Expression result = read("`(a ,b c ,d)");
		Expression expected = read("(join 'a (join b (join 'c (join d nil))))");
		assertEquals(expected, result);
	}

	@Test
	public void testDot1() throws IOException {
		Expression result = read("((a . b) x)");
		assertEquals(list(pair(symbol("a"), symbol("b")), symbol("x")), result);
	}

	@Test
	public void testDot2() throws IOException {
		Expression result = read("(a b . c)");
		assertEquals(pair(symbol("a"), pair(symbol("b"), symbol("c"))), result);
	}

	private static Expression read(String s) throws IOException {
		return new Reader().read(new PushbackReader(new StringReader(s)));
	}
}