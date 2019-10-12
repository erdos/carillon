package io.github.erdos.bellang.reader;

import io.github.erdos.bellang.eval.RT;
import io.github.erdos.bellang.objects.Character;
import io.github.erdos.bellang.objects.Expression;
import io.github.erdos.bellang.objects.Pair;
import io.github.erdos.bellang.objects.Symbol;

import java.io.EOFException;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static io.github.erdos.bellang.eval.RT.list;
import static io.github.erdos.bellang.eval.RT.pair;
import static io.github.erdos.bellang.eval.RT.quote;
import static io.github.erdos.bellang.objects.Symbol.NIL;
import static java.lang.Character.isWhitespace;
import static io.github.erdos.bellang.objects.Symbol.symbol;
import static io.github.erdos.bellang.reader.BackquotedReader.readBackquoted;
import static io.github.erdos.bellang.reader.BackquotedReader.readHole;

public class Reader {

	private final Map<Integer, Pair> sharedPairs = new HashMap<>();

	public static Expression read(PushbackReader pbr) throws IOException {
		if (!canReadExpr(pbr)) {
			return null;
		}

		Expression e = readSymbol(pbr);
		if (e != null) {
			return e;
		}

		e = readQuoted(pbr);
		if (e != null) {
			return e;
		}

		e = readPair(pbr);
		if (e != null) {
			return e;
		}

		e = readFnAbbreviation(pbr);
		if (e != null) {
			return e;
		}

		e = readCharacter(pbr);
		if (e != null) {
			return e;
		}

		e = readBackquoted(pbr);
		if (e != null) {
			return e;
		}

		e = readHole(pbr);
		if (e != null) {
			return e;
		}

		return null;
	}

	private static Expression readFnAbbreviation(PushbackReader pbr) throws IOException {
		if (expectCharacter(pbr, '[')) {
			Expression f = readSymbol(pbr);
			assert f != null;

			skipWhitespaces(pbr);

			Expression underscore = readSymbol(pbr);
			assert underscore != null;

			skipWhitespaces(pbr);

			Expression expr = read(pbr);
			assert expr != null;

			if (!expectCharacter(pbr, ']')) {
				throw new IllegalStateException("Expected ']' character!" + (char) pbr.read());
			}
			return RT.list(Symbol.FN, RT.list(underscore), RT.list(f, underscore, expr));
		} else {
			return null;
		}
	}

	private static Pair readQuoted(PushbackReader pbr) throws IOException {
		if (expectQuote(pbr)) {
			return quote(read(pbr));
		} else {
			return null;
		}
	}

	static Character readCharacter(PushbackReader pbr) throws IOException {
		if (expectBackslash(pbr)) {
			int read = pbr.read();
			if (read == -1) {
				throw new EOFException("EOF while reading character. There is a '\\' at the end of the file!");
			} else {
				return new Character((char) read);
			}
		} else {
			return null;
		}
	}

	static Pair readPair(PushbackReader pbr) throws IOException {
		if (!expectCharacter(pbr, '(')) {
			return null;
		} else if (expectCharacter(pbr, ')')) {
			return Pair.EMPTY;
		} else {
			Deque<Expression> expressions = new LinkedList<>();

			expressions.push(read(pbr));

			skipWhitespaces(pbr);

			Expression last = NIL;
			while (!expectCharacter(pbr, ')')) {
				if (expectCharacter(pbr, '.')) {
					last = read(pbr);
					skipWhitespaces(pbr);
					if (!expectCharacter(pbr, ')')) {
						throw new IllegalStateException("Expecting ) parentheses!");
					} else {
						break;
					}
				} else {
					Expression e = read(pbr);
					skipWhitespaces(pbr);
					expressions.push(e);
				}
			}

			for (Expression e : expressions) {
				assert e != null;
				last = pair(e, last);
			}

			return (Pair) last;
		}
	}

	public static Expression readSymbol(PushbackReader pbr) throws IOException {
		int read = pbr.read();

		if (read == -1) {
			throw new EOFException();
		}

		if (!java.lang.Character.isJavaIdentifierStart(read)) {
			pbr.unread(read);
			return null;
		} else {

			StringBuilder builder = new StringBuilder();
			while (java.lang.Character.isJavaIdentifierPart(read) || read == '|' || read == '.' || read == '!') {
				builder.append((char) read);

				read = pbr.read();
			}

			pbr.unread(read);

			String symbolContents = builder.toString();
			if (symbolContents.contains("|")) {
				String[] parts = symbolContents.split("\\|");
				return list(symbol("t"), symbol(parts[0]), symbol(parts[1]));
			} else if (symbolContents.contains(".")) {
				String[] parts = symbolContents.split("\\.");
				return list(symbol(parts[0]), symbol(parts[1]));
			} else if (symbolContents.contains("!")) {
				String[] parts = symbolContents.split("\\!");
				return list(symbol(parts[0]), quote(symbol(parts[1])));
			} else {
				return symbol(symbolContents);
			}
		}
	}

	private static void skipWhitespaces(PushbackReader pbr) throws IOException {
		int i = ' ';
		while (i != -1 && isWhitespace(i)) {
			i = pbr.read();
		}

		if (i != -1) {
			pbr.unread(i);
		}
	}

	private static boolean canReadExpr(PushbackReader pbr) throws IOException {
		while(true) {
			int i = pbr.read();

			if (i == -1) {
				return false;
			} else if (i == ';') {
				pbr.unread(i);
				skipComment(pbr);
			} else if (java.lang.Character.isWhitespace(i)) {
				skipWhitespaces(pbr);
			} else {
				pbr.unread(i);
				return true;
			}
		}
	}

	private static void skipComment(PushbackReader pbr) throws IOException {
		if (expectCharacter(pbr, ';')) {
			int i = pbr.read();
			while (i != -1 && (char) i != '\n') {
				i = pbr.read();
			}

			skipWhitespaces(pbr);
		}
	}

	static boolean expectCharacter(PushbackReader pbr, char c) throws IOException {
		int i = pbr.read();
		if (i == -1) {
			return false;
		} else if ((char) i != c) {
			pbr.unread(i);
			return false;
		} else {
			return true;
		}
	}

	private static boolean expectBackslash(PushbackReader pbr) throws IOException {
		return expectCharacter(pbr, '\\');
	}

	private static boolean expectQuote(PushbackReader pbr) throws IOException {
		return expectCharacter(pbr, '\'');
	}
}
