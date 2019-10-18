package io.github.erdos.carillon.reader;

import io.github.erdos.carillon.objects.Expression;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SymbolReaderTest {

	@Test
	public void readSymbol1() throws IOException {
		System.out.println(read("asdf"));
	}

	@Test
	public void testSymbolCompose() throws IOException {
		Expression result = read("a:b:c");
		assertEquals(ReaderTest.read("(compose a b c)"), result);
	}

	@Test
	public void testSymbolComplex2() throws IOException {
		Expression result = read("a!b.c");
		assertEquals(ReaderTest.read("(a (quote b) c)"), result);
	}

	@Test
	public void testSymbolComplex() throws IOException {
		Expression result = read("x|~f:g!a");
		assertEquals(ReaderTest.read("(t x ((compose (compose no f) g) (quote a)))"), result);
	}

	@Test
	public void testSymbolComplex1() throws IOException {
		assertEquals(ReaderTest.read("(id (2 x) (3 x))"), ReaderTest.read("(id 2.x 3.x)"));
	}

	@Test
	public void testSymbolComplex3() throws IOException {
		assertEquals(ReaderTest.read("(t c (isa 'cont))"), read("c|isa!cont"));
	}

	private static Expression read(String s) throws IOException {
		return SymbolReader.readSymbol(new PushbackReader(new StringReader(s)));
	}
}