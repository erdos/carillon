package io.github.erdos.bellang.reader;

import io.github.erdos.bellang.eval.RT;
import io.github.erdos.bellang.objects.Character;
import io.github.erdos.bellang.objects.Expression;
import io.github.erdos.bellang.objects.ExpressionVisitor;
import io.github.erdos.bellang.objects.Pair;
import io.github.erdos.bellang.objects.Stream;
import io.github.erdos.bellang.objects.Symbol;

import java.io.IOException;
import java.io.PushbackReader;

import static io.github.erdos.bellang.eval.RT.list;
import static io.github.erdos.bellang.eval.RT.pair;
import static io.github.erdos.bellang.objects.Symbol.JOIN;
import static io.github.erdos.bellang.objects.Symbol.NIL;
import static io.github.erdos.bellang.objects.Symbol.symbol;
import static io.github.erdos.bellang.reader.Reader.expectCharacter;

final class BackquotedReader {

	static final Symbol UNQUOTE = symbol("unquote");

	// TODO: not yet supported!!!
	static final Symbol UNQUOTE_SLICING = symbol("x-spliced");


	static Expression readBackquoted(PushbackReader pbr) throws IOException {
		if (!expectCharacter(pbr, '`')) {
			return null;
		} else {
			return walk(Reader.read(pbr));
		}
	}

	static Expression readHole(PushbackReader pbr) throws IOException {
		if (!expectCharacter(pbr, ',')) {
			return null;
		} else {
			boolean spliced = expectCharacter(pbr, '@');
			Expression expr = Reader.read(pbr);
			assert expr != null;
			return pair(spliced ? UNQUOTE_SLICING : UNQUOTE, expr);
		}
	}

	private static Expression walk(Expression e) {
		if (e == null) return null; // TODO: remove nul values!
		return e.apply(new ExpressionVisitor<Expression>() {
			@Override
			public Expression pair(Pair pair) {
				if (pair.car() == UNQUOTE) {
					return pair.cdr();
				} else if (  (pair.car() instanceof Pair) && (((Pair)pair.car()).car() == UNQUOTE_SLICING)) {
					if (pair.cdr() == NIL) {
						return ((Pair) pair.car()).cdr();
					} else {
						return RT.pair(Symbol.symbol("append"), RT.pair(((Pair) pair.car()).cdr(), RT.list(walk(pair.cdr()))));
					}
				} else {
					return list(JOIN, walk(pair.car()), walk(pair.cdr()));
				}
			}

			@Override
			public Expression stream(Stream stream) {
				return stream;
			}

			@Override
			public Expression symbol(Symbol symbol) {
				if (symbol == NIL) {
					return symbol;
				} else {
					return RT.quote(symbol);
				}
			}

			@Override
			public Expression character(Character character) {
				return character;
			}
		});
	}
}
