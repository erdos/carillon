package io.github.erdos.bellang.objects;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class Symbol implements Expression {

	private static final ReferenceQueue rq = new ReferenceQueue();
	private static final Map<String, Reference<Symbol>> table = new ConcurrentHashMap<>();

	public static final Symbol QUOTE = symbol("quote");
	public static final Symbol T = symbol("t");
	public static final Symbol NIL = symbol("nil");
	public static final Symbol O = symbol("o");

	public static final Symbol CHARS = symbol("chars");
	public static final Symbol GLOBE = symbol("globe");
	public static final Symbol SCOPE = symbol("scope");
	public static final Symbol INS = symbol("ins");
	public static final Symbol OUTS = symbol("outs");

	public static final Symbol LIT = symbol("lit");


	public static final Symbol IF = symbol("if");
	public static final Symbol APPLY = symbol("apply");

	public static final Symbol ID = symbol("id");

	public static final Symbol JOIN = symbol("join");
	public static final Symbol CAR = symbol("car");
	public static final Symbol CDR = symbol("cdr");


	public static final Symbol TYPE = symbol("type");

	public static final Symbol SYMBOL = symbol("symbol");
	public static final Symbol PAIR = symbol("pair");
	public static final Symbol CHAR = symbol("char");
	public static final Symbol STREAM = symbol("stream");


	public static final Symbol XAR = symbol("xar");
	public static final Symbol CXR = symbol("cxr");

	public static final Symbol SYM = symbol("sym");

	public static final Symbol NOM = symbol("nom");

	public static final Symbol COIN = symbol("coin");

	public static final Symbol SYS = symbol("sys");


	public static final Symbol CLO = symbol("clo"); // closure
	public static final Symbol MAC = symbol("mac"); // macro expression

	public static final Symbol FN = symbol("fn");

	public static final Symbol SET = symbol("set");

	public static Symbol symbol(String name) {
		Symbol symbol;
		do {
			if (rq.poll() != null) {
				while (rq.poll() != null) {
					// empty intentionally.
				}

				for (Map.Entry<String, Reference<Symbol>> e : table.entrySet()) {
					Reference<Symbol> val = e.getValue();
					if (val != null && val.get() == null) table.remove(e.getKey(), val);
				}
			}
			symbol = table.computeIfAbsent(name, k -> new WeakReference<>(new Symbol(name))).get();
		} while (symbol == null);
		return symbol;
	}

	public final String name;

	private Symbol(String name) {this.name = name;}

	public String toString() {
		return name;
	}

	@Override
	public <T> T apply(ExpressionVisitor<T> visitor) {
		return visitor.symbol(this);
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
