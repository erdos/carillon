package io.github.erdos.carillon.reader;

import io.github.erdos.carillon.objects.Character;
import io.github.erdos.carillon.objects.Expression;
import io.github.erdos.carillon.objects.Pair;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import static io.github.erdos.carillon.eval.RT.list;
import static io.github.erdos.carillon.eval.RT.pair;
import static io.github.erdos.carillon.eval.RT.quote;
import static io.github.erdos.carillon.objects.Character.character;
import static io.github.erdos.carillon.objects.Symbol.NIL;
import static io.github.erdos.carillon.objects.Symbol.symbol;
import static org.junit.jupiter.api.Assertions.*;

class ReaderTest {

	@Test
	public void testReadCharacter() throws IOException {
		Expression result = read("\\a");
		Character expected = Character.character('a');
		assertEquals(expected, result);
	}

	@Test
	public void testReadPairEmpty() throws IOException {
		assertEquals(NIL, read("()"));
		assertEquals(NIL, read("(\n)"));
	}

	@Test
	public void testReadPairOne() throws IOException {
		Expression result = read("(\\x)");
		Pair expected = pair(Character.character('x'), NIL);
		assertEquals(expected, result);
	}

	@Test
	public void testPairTwo() throws IOException {
		Expression result = read("(\\x \\y)");
		Pair expected = pair(character('x'), pair(character('y'), NIL));
		assertEquals(expected, result);
	}

	@Test
	public void testSymbolLong1() throws IOException {
		assertEquals((char) 7, ((Character)read("\\bel")).getChar());
	}

	@Test
	public void testPairTwoDot() throws IOException {
		Expression result = read("(\\x . \\y)");
		Pair expected = pair( character('x'), character('y'));
		assertEquals(expected, result);
	}

	@Test
	public void testFnShorthandLong() throws IOException {
		assertEquals(read("(fn (_) (f _ x))"), read("[f _ x]"));
		assertEquals(read("(fn (_) (no z))"), read("[no z]"));
		assertEquals(read("(fn (_) (no z w t))"), read("[no z w t]"));
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
	public void testReadBackticket2() throws IOException {
		Expression result = read("`(a b ,@ds)");
		Expression expected = read("(join 'a (join 'b ds))");
		assertEquals(expected, result);
	}

	@Test
	public void testReadBackticket3() throws IOException {
		Expression result = read("`(a ,@db c)");
		Expression expected = read("(join 'a (append db (join 'c nil)))");
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

	@Test
	public void readString() throws IOException {
		assertEquals(read("'(\\a \\b \\c)"), read("\"abc\""));
		assertEquals(read("'(\\f \\o \\o \\\" \\b \\a \\r)"), read("\"foo\\\"bar\""));
		assertEquals(read("'()"), read("\"\""));
	}

	static Expression read(String s) throws IOException {
		return new Reader().read(new PushbackReader(new StringReader(s)));
	}
}