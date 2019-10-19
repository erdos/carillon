package io.github.erdos.carillon.reader;

import io.github.erdos.carillon.eval.RT;
import io.github.erdos.carillon.objects.Expression;
import io.github.erdos.carillon.objects.Pair;
import io.github.erdos.carillon.objects.Symbol;

import java.io.EOFException;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static io.github.erdos.carillon.eval.RT.list;
import static io.github.erdos.carillon.eval.RT.pair;
import static io.github.erdos.carillon.objects.Pair.collectPairOrNil;
import static io.github.erdos.carillon.objects.Symbol.LIT;
import static io.github.erdos.carillon.objects.Symbol.NIL;
import static io.github.erdos.carillon.objects.Symbol.T;
import static io.github.erdos.carillon.objects.Symbol.symbol;

final class SymbolReader {

	private SymbolReader() {}

	static Expression readSymbol(PushbackReader pbr) throws IOException {
		String read = readUntilDelimiter(pbr);
		if (read == null) {
			return null;
		} else if (Pattern.matches("[\\+\\-]?\\d+", read)) {
			return toInteger(read);
		} else {
			return split(read);
		}
	}

	static Pair toInteger(String read) {
		int number = Integer.valueOf(read);
		Symbol sign = number < 0 ? symbol("-") : symbol("+");

		Expression nth = IntStream.range(0, Math.abs(number)).mapToObj(x -> T).collect(collectPairOrNil());

		Pair realPart = list(sign, nth, list(T));
		Pair complexPart = list(symbol("+"), NIL, list(T));
		return list(LIT, symbol("num"), realPart, complexPart);
	}

	static String readUntilDelimiter(PushbackReader pbr) throws IOException {
		int read = pbr.read();

		if (read == -1) {
			throw new EOFException();
		}

		if (!identifierStart((char) read)) {
			pbr.unread(read);
			return null;
		} else {

			StringBuilder builder = new StringBuilder();
			while (read != -1 && identifierPart((char) read)) {
				builder.append((char) read);

				read = pbr.read();
			}

			pbr.unread(read);

			return builder.toString();
		}
	}

	private static boolean intrac(char c) {
		return c == '.' || c == '!';
	}

	private static boolean signc(char c) {
		return c == '+' || c == '-';
	}

	private static Expression split(String cs) {
		if (cs.contains("|")) {
			String[] parts = cs.split("\\|");

			Expression c = intrasymbolColons(parts[0]);
			Expression e = split(parts[1]);
			return list(T, c, e);
		} else if (cs.contains(".") || cs.contains("!")) {
			String[] parts = cs.split("(\\.|\\!)", 2);

			Expression head = intrasymbolColons(parts[0]);



			Expression tail;
			if (parts[1].contains(".") || parts[1].contains("!")) {
				tail = split(parts[1]);
			} else {
				tail = list(intrasymbolColons(parts[1]));
			}

			if (cs.indexOf("!") != -1 && cs.indexOf("!") <= parts[0].length()) {
				tail = pair( RT.quote(((Pair)tail).car()), ((Pair)tail).cdr());
			}


			return pair(head, tail);

		} else {
			return intrasymbolColons(cs);
		}
	}

	private static Expression intrasymbolColons(String s) {
		if (s.contains(":")) {
			String[] parts = s.split("\\:");

			Expression tail = NIL;
			for (int i = parts.length - 1; i >= 0; i--) {
				tail = pair(prependedTildes(parts[i]), tail);
			}
			return RT.pair(symbol("compose"), tail);

		} else {
			return prependedTildes(s);
		}
	}

	private static Expression prependedTildes(String s) {
		if (s.startsWith("~")) {
			return RT.list(symbol("compose"), symbol("no"), prependedTildes(s.substring(1)));
		} else {
			return symbol(s);
		}
	}

	static boolean identifierStart(char read) {
		return ! (java.lang.Character.isWhitespace(read) || read == '"' || read == '`' || read == '\'' || read == '@' || read == ',' || read == '(' || read == '[' || read == ')' || read == ']' || read == ';' || read == '\\');
	}

	static boolean identifierPart(char read) {
		return identifierStart(read);
	}
}
