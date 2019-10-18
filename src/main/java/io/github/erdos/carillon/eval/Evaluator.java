package io.github.erdos.carillon.eval;

import io.github.erdos.carillon.objects.Expression;
import io.github.erdos.carillon.objects.Pair;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class Evaluator {

	private final Map<Variable, Expression> globals = new ConcurrentHashMap<>();

	private final ThreadLocal<Map<Variable, Expression>> dynamicBindings = ThreadLocal.withInitial(HashMap::new);

	/**
	 * If you do an assignment to a variable that has one of the three kinds
	 * of bindings, you'll modify whichever binding is currently visible. If
	 * you do an assignment to a variable that's not bound, you'll create a
	 * global binding for it.
	 */
	public void set(Variable v, Expression e) {
		if (dynamicBindings.get().containsKey(v)) {
			dynamicBindings.get().put(v, e);
		} else if (getLexicalBinding(v) != null) {
			// TODO: implementation here.
		} else {
			globals.put(v, e);
		}
	}

	/**
	 * Dynamic bindings take precendence over lexical bindings, which take precedence over global ones.
	 */
	public Optional<Expression> get(Variable v) {
		Expression e = getDynamicBinding(v);
		if (e != null) return Optional.of(e);

		e = getLexicalBinding(v);
		if (e != null) return Optional.of(e);

		e = getGlobalBinding(v);
		if (e != null) return Optional.of(e);

		return Optional.empty();
	}

	public Expression getGlobalBinding(Variable v) {
		return globals.get(v);
	}

	public Expression getLexicalBinding(Variable v) {
		for (Map<Variable, Expression> m : lexicals) {
			if (m.containsKey(v)) {
				return m.get(v);
			}
		}
		return null;
	}

	public Map<Variable, Expression> getLexicalScope() {
		Map<Variable, Expression> map = new HashMap<>();
		for (Map<Variable, Expression> m : lexicals) {
			for (Map.Entry<Variable, Expression> entry : m.entrySet()) {
				map.putIfAbsent(entry.getKey(), entry.getValue());
			}
		}
		return map;
	}

	public Expression getDynamicBinding(Variable v) {
		return dynamicBindings.get().get(v);
	}


	private final Deque<Map<Variable, Expression>> lexicals = new ArrayDeque<>();

	public Expression withLexicals(Map<Variable, Expression> lexicalMapping, Supplier<Expression> body) {
		lexicals.push(lexicalMapping);
		try {
			return body.get();
		} finally {
			lexicals.pop();
		}
	}

	public Expression getGlobe() {
		return globals.entrySet()
				.stream()
				.map(entry -> RT.pair(entry.getKey().getExpression(), entry.getValue()))
				.collect(Pair.collectPairOrNil());
	}

	public Expression getScope() {
		return getLexicalScope()
				.entrySet()
				.stream()
				.map(entry -> RT.pair(entry.getKey().getExpression(), entry.getValue()))
				.collect(Pair.collectPairOrNil());
	}
}
