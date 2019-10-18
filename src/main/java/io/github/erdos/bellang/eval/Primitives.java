package io.github.erdos.bellang.eval;

import io.github.erdos.bellang.objects.Character;
import io.github.erdos.bellang.objects.Expression;
import io.github.erdos.bellang.objects.ExpressionVisitor;
import io.github.erdos.bellang.objects.Pair;
import io.github.erdos.bellang.objects.Stream;
import io.github.erdos.bellang.objects.Symbol;

import static io.github.erdos.bellang.objects.Symbol.NIL;

public class Primitives {

	Expression evalId(Pair id, ExpressionEvaluatorVisitor evaluator) {
		assert Symbol.ID.equals(id.car());
		Pair args = (Pair) id.cdr();

		if (evaluator.appliedTo(args.car()) == evaluator.appliedTo(((Pair) args.cdr()).car())) {
			return Symbol.T;
		} else {
			return NIL;
		}
	}

	Expression evalJoin(Pair id, ExpressionEvaluatorVisitor evaluator) {
		assert Symbol.JOIN.equals(id.car());

		if (id.isRightNil()) {
			return Pair.EMPTY;
		} else {
			Pair args = (Pair) id.cdr();
			if (args.cdr() == NIL) {
				return new Pair(evaluator.appliedTo(args.car()), NIL);
			} else {
				return new Pair(evaluator.appliedTo(args.car()), evaluator.appliedTo(args.cadr()));
			}
		}
	}

	Expression evalCar(Pair pair, ExpressionEvaluatorVisitor evaluator) {
		assert Symbol.CAR.equals(pair.car());
		Expression result = evaluator.appliedTo(pair.cadr());
		if (result == NIL) {
			return NIL;
		} else if (result instanceof Pair) {
			return ((Pair) result).car();
		} else {
			throw new EvaluationException(result, "You can call (car .) only on a pair or nil!");
		}
	}

	Expression evalCdr(Pair pair, ExpressionEvaluatorVisitor evaluator) {
		assert Symbol.CDR.equals(pair.car());
		Expression result = evaluator.appliedTo(pair.cadr());
		if (result == NIL) {
			return NIL;
		} else if (result instanceof Pair) {
			return ((Pair) result).cdr();
		} else {
			throw new EvaluationException(result, "You can call (cdr .) only on a pair or nil.");
		}
	}

	Symbol evalType(Pair pair, ExpressionEvaluatorVisitor evaluator) {
		assert Symbol.TYPE.equals(pair.car());

		return evaluator.appliedTo(pair.cadr()).apply(new ExpressionVisitor<Symbol>() {
			@Override
			public Symbol pair(Pair pair) {
				return Symbol.PAIR;
			}

			@Override
			public Symbol stream(Stream stream) {
				return Symbol.STREAM;
			}

			@Override
			public Symbol symbol(Symbol symbol) {
				return Symbol.SYMBOL;
			}

			@Override
			public Symbol character(Character character) {
				return Symbol.CHAR;
			}
		});
	}

	Expression evalXar(Pair pair, ExpressionEvaluatorVisitor evaluator) {
		Expression newValue = pair.cadr().apply(evaluator);
		pair.setCar(newValue);
		return newValue;
	}

	Expression evaCxr(Pair pair, ExpressionEvaluatorVisitor evaluator) {
		Expression newValue = pair.cadr().apply(evaluator);
		pair.setCdr(newValue);
		return newValue;
	}

	Symbol evalSym(Pair pair, ExpressionEvaluatorVisitor evaluator) {
		StringBuilder builder = new StringBuilder();
		((Pair) pair.cadr().apply(evaluator)).forEach(c -> builder.append(((Character) c).getChar()));
		return Symbol.symbol(builder.toString());
	}

	Pair evalNom(Pair pair, ExpressionEvaluatorVisitor evaluator) {
		assert Symbol.NOM.equals(pair.car());
		if (pair.cdr() == NIL) {
			throw new EvaluationException.WrongArityException(NIL, NIL);
		} else if (((Pair) pair.cdr()).cdr() != NIL) {
			throw new EvaluationException.WrongArityException(NIL, NIL);
		} else {
			return ((Symbol) evaluator.appliedTo(pair.cadr()))
					.name
					.chars()
					.mapToObj(x -> Character.character((char) x)).collect(Pair.collect());
		}
	}

	//	 Returns either t or nil randomly.
	Symbol coin(Pair pair) {
		assert Symbol.COIN.equals(pair.car());
		return (Math.random() < 0.5) ? null : Symbol.T;
	}

	// TODO: what is the return value here?
	Expression sys(Pair pair, ExpressionEvaluatorVisitor evaluator) {
		assert Symbol.SYS.equals(pair.car());
		throw new EvaluationException.FeatureNotImplementedException(Symbol.SYS);
	}

	/**
	 *
	 10. (wrb x y)

	 Writes the bit x (represented by either \1 or \0) to the stream y.
	 Returns x. Signals an error if it can't or if x is not \1 or \0. If y
	 is nil, writes to the initial output stream.


	 11. (rdb x)

	 Tries to read a bit from the stream x. Returns \1 or \0 if it finds
	 one, nil if no bit is currently available, or eof if no more will be
	 available. Signals an error if it can't. If x is nil, reads from the
	 initial input stream.


	 12. (ops x y)

	 Returns a stream that writes to or reads from the place whose name is
	 the string x, depending on whether y is out or in respectively.
	 Signals an error if it can't, or if y is not out or in.


	 13. (cls x)

	 Closes the stream x. Signals an error if it can't.


	 14. (stat x)

	 Returns either closed, in, or out depending on whether the stream x
	 is closed, or reading from or writing to something respectively.
	 Signals an error if it can't.


	 16. (sys x)

	 Sends x as a command to the operating system.

	 */
}
