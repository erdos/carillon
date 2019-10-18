package io.github.erdos.carillon.objects;

import io.github.erdos.carillon.eval.EvaluationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static io.github.erdos.carillon.eval.RT.list;
import static io.github.erdos.carillon.eval.RT.pair;
import static io.github.erdos.carillon.eval.RT.quote;
import static io.github.erdos.carillon.objects.Symbol.NIL;
import static io.github.erdos.carillon.objects.Symbol.symbol;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PairTest {

	@Test
	public void testIterable() {
		Pair pair = list(symbol("a"), symbol("b"), symbol("c"));

		List<Expression> xs = new LinkedList<>();
		pair.forEach(xs::add);

		assertEquals(Arrays.asList(symbol("a"), symbol("b"), symbol("c")), xs);
	}

	@Test
	public void testToStringQuote() {
		assertEquals("'c", quote(symbol("c")).toString());
	}

	@Test
	public void testToString1() {
		assertEquals("(c)", pair(symbol("c"), NIL).toString());
		assertEquals("(nil)", pair(NIL, NIL).toString());
		assertEquals("(nil . c)", pair(NIL, symbol("c")).toString());
		assertEquals("(c . d)", pair(symbol("c"), symbol("d")).toString());
		assertEquals("(c d)", list(symbol("c"), symbol("d")).toString());
	}

	@Test
	public void testToString2List() {
		assertEquals("(b c d)", list(symbol("b"), symbol("c"), symbol("d")).toString());
	}

	@Test
	public void testToStringListEnd() {
		Pair ef = pair(symbol("e"), symbol("f"));
		Pair input = pair(symbol("c"), pair(symbol("d"), ef));
		assertEquals("(c d e . f)", input.toString());
	}

	@Test
	public void testCollect() {
		Pair p = Stream
				.of(symbol("a"), symbol("b"), symbol("c"))
				.collect(Pair.collect());
		assertEquals(symbol("a"), p.nth(0));
		assertEquals(symbol("b"), p.nth(1));
		assertEquals(symbol("c"), p.nth(2));
	}

	@Test()
	public void testCollectEmpty() {
		assertThrows(EvaluationException.class, () -> new ArrayList<Expression>().stream().collect(Pair.collect()));

		assertEquals(NIL, new ArrayList<Expression>().stream().collect(Pair.collectPairOrNil()));
	}
}