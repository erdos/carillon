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

public class Environment {

	private final Map<Variable, Pair> globals = new ConcurrentHashMap<>();
	private final Deque<Map<Variable, Pair>> lexicals = new ArrayDeque<>();
	private final ThreadLocal<Map<Variable, Expression>> dynamicBindings = ThreadLocal.withInitial(HashMap::new);

	private final ThreadLocal<LastLocation> lastLocation = ThreadLocal.withInitial(() -> null);

	// adds support to (where x) calls.
	static final class LastLocation {
		Pair pair;
		boolean car;

		LastLocation(Pair p, boolean c) {
			this.pair = p;
			this.car = c;
		}

		void update(Expression value) {
			if (car) {
				pair.setCar(value);
			} else {
				pair.setCdr(value);
			}
		}
	}

	public Expression whereCar(Pair p) {
		lastLocation.set(new LastLocation(p, true));
		return p.car();
	}

	public Expression whereCdr(Pair p) {
		lastLocation.set(new LastLocation(p, false));
		return p.cdr();
	}

	public void whereClear() {
		lastLocation.remove();
	}

	public Optional<LastLocation> getLastLocation() {
		return Optional.ofNullable(lastLocation.get());
	}

	/**
	 * If you do an assignment to a variable that has one of the three kinds
	 * of bindings, you'll modify whichever binding is currently visible. If
	 * you do an assignment to a variable that's not bound, you'll create a
	 * global binding for it.
	 */
	public void set(Variable v, Expression e) {
		if (dynamicBindings.get().containsKey(v)) {
			dynamicBindings.get().put(v, e);
		} else if (swapLexicalBinding(v, e)) {
			// already swapped, we are all good.
		} else {
			globals.put(v, new Pair(v.getExpression(), e));
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
		if (globals.containsKey(v)) {
			return whereCdr(globals.get(v));
		} else {
			return null;
		}
	}

	public Expression getLexicalBinding(Variable v) {
		for (Map<Variable, Pair> m : lexicals) {
			if (m.containsKey(v)) {
				return whereCdr(m.get(v));
			}
		}
		return null;
	}

	public boolean swapLexicalBinding(Variable v, Expression newValue) {
		for (Map<Variable, Pair> m : lexicals) {
			if (m.containsKey(v)) {
				m.get(v).setCdr(newValue);
				return true;
			}
		}
		return false;
	}

	public Expression getDynamicBinding(Variable v) {
		return dynamicBindings.get().get(v);
	}

	public Expression withLexicals(Map<Variable, Pair> lexicalMapping, Supplier<Expression> body) {
		lexicals.push(lexicalMapping);
		try {
			return body.get();
		} finally {
			lexicals.pop();
		}
	}

	public Expression withDynamicBinding(Variable variable, Expression value, Supplier<Expression> body) {
		Expression valueBefore = dynamicBindings.get().get(variable);

		dynamicBindings.get().put(variable, value);

		try {
			return body.get();
		} finally {
			if (valueBefore != null) {
				dynamicBindings.get().put(variable, valueBefore);
			} else {
				dynamicBindings.get().remove(variable);
			}
		}
	}

	public Expression getGlobe() {
		return globals.values().stream().collect(Pair.collectPairOrNil());
	}

	public Expression getScope() {
		Map<Variable, Expression> map = new HashMap<>();
		for (Map<Variable, Pair> m : lexicals) {
			for (Map.Entry<Variable, Pair> entry : m.entrySet()) {
				map.putIfAbsent(entry.getKey(), entry.getValue());
			}
		}

		return map.values().stream().collect(Pair.collectPairOrNil());
	}
}
