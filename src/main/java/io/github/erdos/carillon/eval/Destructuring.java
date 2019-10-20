package io.github.erdos.carillon.eval;

import io.github.erdos.carillon.eval.EvaluationException.WrongArityException;
import io.github.erdos.carillon.objects.Expression;
import io.github.erdos.carillon.objects.Pair;
import io.github.erdos.carillon.objects.Symbol;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static io.github.erdos.carillon.eval.EvaluationException.evalException;
import static io.github.erdos.carillon.objects.Symbol.NIL;
import static io.github.erdos.carillon.objects.Symbol.O;
import static io.github.erdos.carillon.objects.Symbol.T;

public final class Destructuring {

	private Destructuring() {}

	public static void destructureArgs(Expression name, Expression value, Function<Expression, Expression> mapper, Map<Variable, Pair> result) {
		if (name == NIL && value != NIL) {
			throw new WrongArityException(NIL, value);
		} else if (name == NIL) {
			// empty
		} else if (name instanceof Pair && ((Pair) name).car() instanceof Pair && ((Pair) ((Pair) name).car()).car() == O && value == NIL) {
			destructureOptionalsTail((Pair) name, result, mapper);
		} else if (value == NIL && name instanceof Symbol) {
			result.put(Variable.enforce(name), new Pair(name, value));
		} else if (value == NIL) {
			throw new WrongArityException(name, NIL);
		} else if (name instanceof Symbol) {
			result.put(Variable.enforce(name), new Pair(name, value));
		} else {
			destructurePairs((Pair) name, (Pair) value, result, mapper);
		}
	}


	public static void destructureOptionalsTail(Pair seqOfOptionals, Map<Variable, Pair> mappings, Function<Expression, Expression> mapper) {
		seqOfOptionals.forEach(opt -> destructureOptional((Pair) opt, Optional.empty(), mappings, mapper));
	}

	protected static void destructure(Expression name, Expression value, Map<Variable, Pair> mappings, Function<Expression, Expression> mapper) {

		if (name == NIL) {

			if (value == NIL) {
				return;
			}

			throw new EvaluationException(NIL, "Too many arguments supplied!");
		}

		Optional<Variable> var = Variable.of(name);

		if (var.isPresent()) {
			mappings.put(var.get(), new Pair(var.get().getExpression(), value));
		} else {
			Pair namePair = (Pair) name;

			if (namePair.car() == O) {
				destructureOptional(namePair, Optional.of(value), mappings, mapper);
			} else if (namePair.car() == T) {
				destructureTyped(namePair, value, mappings, mapper);
			} else if (namePair.car().equals(Pair.EMPTY)) {
				mappings.put(Variable.enforce(namePair), new Pair(namePair, value));
			} else if (namePair.car() instanceof Pair && ((Pair) namePair.car()).car() == O && !(value instanceof Pair)) {
				// TODO: optional shall be diff?
				destructureOptional((Pair) namePair.car(), Optional.of(value), mappings, mapper);
			} else {
				destructurePairs(namePair, (Pair) value, mappings, mapper);
			}
		}
	}

	private static void destructurePairs(Pair name0, Pair value0, Map<Variable, Pair> mappings, Function<Expression, Expression> mapper) {
		Expression nameIterator = name0;
		Optional<Expression> valueIterator = Optional.of(value0);

		while (nameIterator instanceof Pair) {

			Pair namePair = (Pair) nameIterator;

			if (namePair.car() == O) {
				if (valueIterator.map(x->(Pair) x).map(Pair::cdr).map(x -> x == NIL ? null : x).isPresent()) {
					throw new EvaluationException(NIL, "Too many args for optional!");
				}

				Optional<Expression> value = valueIterator.map(x->(Pair) x).map(Pair::car);
				destructureOptional(namePair, value, mappings, mapper);
			} else if (namePair.car() == T) {
				if (valueIterator.map(x->(Pair) x).map(Pair::cdr).map(x -> x == NIL ? null : x).isPresent()) {
					throw new WrongArityException(NIL, NIL);
				} else {
					destructureTyped(namePair, valueIterator.orElseThrow(evalException(NIL, "Missing value!")), mappings, mapper);
				}
			} else if (namePair.car().equals(Pair.EMPTY)) {
				mappings.put(Variable.enforce(namePair), valueIterator.map(value -> new Pair(namePair, value)).orElseThrow(evalException(NIL, "Missing value for binding!")));
				return;
			} else if (namePair.car() instanceof Pair && ((Pair) namePair.car()).car() == O) {
				destructureOptional((Pair) namePair.car(), valueIterator.map(x->(Pair) x).map(Pair::car), mappings, mapper);
			} else if (valueIterator.orElse(null) instanceof Symbol) {
				destructure(nameIterator, valueIterator.get(), mappings, mapper);
				return;
			} else {
				destructure(namePair.car(), valueIterator.map(x->(Pair) x).map(Pair::car).orElseThrow(() -> new WrongArityException(NIL, NIL)), mappings, mapper);
			}

			nameIterator = ((Pair) nameIterator).cdr();
			valueIterator = valueIterator.map(x->(Pair) x).map(Pair::cdr).map(x -> x == NIL ? null : x);
		}

		if (nameIterator != NIL) { // last item is a symbol!
			destructure(nameIterator, valueIterator.orElse(NIL), mappings, mapper);
		} else if (valueIterator.isPresent()) {
			throw new WrongArityException(NIL, NIL);
		}
	}

	private static void destructureTyped(Pair name, Expression value, Map<Variable, Pair> mappings, Function<Expression, Expression> mapper) {
		assert name.car() == T;
		destructure(name.cadr(), value, mappings, mapper);
	}

	private static void destructureOptional(Pair name, Optional<Expression> value, Map<Variable, Pair> mappings, Function<Expression, Expression> mapper) {
		assert name.car() == O;
		Expression actualValue = value.orElse(mapper.apply(name.nthOrNil(2)));
		destructure(name.cadr(), actualValue, mappings, mapper);
	}
}
