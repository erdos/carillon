package io.github.erdos.bellang.eval;

import io.github.erdos.bellang.objects.Expression;
import io.github.erdos.bellang.objects.Symbol;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Evaluator {

	private final Map<String, Expression> globals = new ConcurrentHashMap<>();

	private final ThreadLocal<Map<String, Expression>> dynamicBindings = ThreadLocal.withInitial(HashMap::new);

	/**
	 * If you do an assignment to a variable that has one of the three kinds
	 * of bindings, you'll modify whichever binding is currently visible. If
	 * you do an assignment to a variable that's not bound, you'll create a
	 * global binding for it.
	 */
	public void set(Symbol s, Expression e) {
		// System.out.println("Setting " + s + " = " + e);
		if (dynamicBindings.get().containsKey(s.name)) {
			dynamicBindings.get().put(s.name, e);
		} else if (getLexicalBinding(s) != null) {
			// TODO: implementation here.
		} else {
			globals.put(s.name, e);
		}
	}

	/**
	 * Dynamic bindings take precendence over lexical bindings, which take precedence over global ones.
	 */
	public Expression get(Symbol s) {
		Expression e = getDynamicBinding(s);
		if (e != null) return e;

		e = getLexicalBinding(s);
		if (e != null) return e;

		e = getGlobalBinding(s);
		if (e != null) return e;

		throw new EvaluationException(s, "No binding for symbol " + s);
	}

	public Expression getGlobalBinding(Symbol s) {
		return globals.get(s.name);
	}

	public Expression getLexicalBinding(Symbol s) {

		// TODO: itt lehet, hogy forditva kellene berani!
		for (Map<String, Expression> m : lexicals) {
			if (m.containsKey(s.name)) {
				return m.get(s.name);
			}
		}

		return null;
	}

	public Map<String, Expression> getLexicalScope() {
		Map<String, Expression> map = new HashMap<>();
		for (Map<String, Expression> m : lexicals) {
			for (Map.Entry<String, Expression> entry : m.entrySet()) {
				map.putIfAbsent(entry.getKey(), entry.getValue());
			}
		}
		return map;
	}

	public Expression getDynamicBinding(Symbol s) {
		return dynamicBindings.get().get(s.name);
	}


	private final Deque<Map<String, Expression>> lexicals = new ArrayDeque<>();

	public void pushLexicals(Map<String, Expression> lexicalMapping) {
		lexicals.push(lexicalMapping);
	}

	public void popLexicals() {
		lexicals.pop();
	}
}
