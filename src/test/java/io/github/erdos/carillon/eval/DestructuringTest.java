package io.github.erdos.carillon.eval;

import io.github.erdos.carillon.objects.Expression;
import io.github.erdos.carillon.objects.Pair;
import io.github.erdos.carillon.reader.Reader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.erdos.carillon.eval.RT.list;
import static io.github.erdos.carillon.eval.Variable.enforce;
import static io.github.erdos.carillon.objects.Symbol.NIL;
import static io.github.erdos.carillon.objects.Symbol.symbol;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DestructuringTest {

	@Test
	public void testSimpleSymbol() {
		Map<Variable, Expression> expected = singletonMap(enforce(symbol("a")), symbol("aa"));
		assertEquals(expected, destructure(symbol("a"), symbol("aa"), x -> x));
	}

	@Test
	public void testIntoSymbol() {
		Map<Variable, Expression> result = destructure(read("args"), read("(aa bb cc)"), x -> x);
		assertEquals(read("(aa bb cc)"), result.get(var("args")));
	}

	@Test
	public void testIntoSymbolNil() {
		Map<Variable, Expression> result = destructure(read("args"), NIL, x -> x);
		assertEquals(NIL, result.get(var("args")));
	}

	@Test
	public void testTopLevelOptionalUnprovided() {
		Map<Variable, Expression> result = destructure(read("(o xs 'a)"), NIL, x -> x);
		assertEquals(NIL, result.get(var("xs")));
	}

	@Test
	public void testTopLevelVariable() {
		Expression variable = list(list(NIL), NIL);
		Map<Variable, Expression> result = destructure(variable, symbol("a"), x -> x);
		assertEquals(symbol("a"), result.get(Variable.enforce(variable)));
	}

	@Test
	public void testTopLevelOptionalProvided() {
		Map<Variable, Expression> result = destructure(read("(o xs 'a)"), symbol("b"), x -> x);
		assertEquals(symbol("b"), result.get(var("xs")));
	}

	@Test
	public void testSimpleSymbol2() {
		Map<Variable, Expression> result = destructure(read("(a b c)"), read("(aa bb cc)"), x -> x);
		assertEquals(symbol("aa"), result.get(var("a")));
		assertEquals(symbol("bb"), result.get(var("b")));
		assertEquals(symbol("cc"), result.get(var("c")));
	}

	@Test
	public void testBindingRest() {
		Map<Variable, Expression> result = destructure(read("(a . x)"), read("(aa bb cc)"), x -> x);
		assertEquals(symbol("aa"), result.get(var("a")));
		assertEquals(read("(bb cc)"), result.get(var("x")));
	}

	@Test
	public void testDestructurePair() {
		Map<Variable, Expression> result = destructure(read("(a . b)"), read("(x . y)"), x -> x);
		assertEquals(symbol("x"), result.get(var("a")));
		assertEquals(symbol("y"), result.get(var("b")));
	}

	@Test
	public void testBindRestToNil() {
		Map<Variable, Expression> result = destructure(read("(a . x)"), read("(aa)"), x -> x);
		assertEquals(symbol("aa"), result.get(var("a")));
		assertEquals(NIL, result.get(var("x")));
	}

	@Test
	public void testBindOptional() {
		Map<Variable, Expression> result = destructure(read("(a (o x))"), read("(aa)"), x -> x);
		assertEquals(symbol("aa"), result.get(var("a")));
		assertEquals(NIL, result.get(var("x")));
	}

	static Variable var(String name) {
		return Variable.enforce(symbol(name));
	}

	static Expression read(String s) {
		try {
			return new Reader().read(new PushbackReader(new StringReader(s)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Map<Variable, Expression> destructure(Expression name, Expression value, Function<Expression, Expression> mapper) {
		Map<Variable, Pair> result = new HashMap<>();
		Destructuring.destructure(name, value, result, mapper);
		return result.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().cdr()));
	}
}