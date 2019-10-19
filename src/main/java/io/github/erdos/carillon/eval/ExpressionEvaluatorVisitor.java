package io.github.erdos.carillon.eval;

import io.github.erdos.carillon.eval.EvaluationException.UnboundSymbolException;
import io.github.erdos.carillon.objects.Character;
import io.github.erdos.carillon.objects.Expression;
import io.github.erdos.carillon.objects.ExpressionVisitor;
import io.github.erdos.carillon.objects.Pair;
import io.github.erdos.carillon.objects.Stream;
import io.github.erdos.carillon.objects.Symbol;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.erdos.carillon.eval.EvaluationException.evalException;
import static io.github.erdos.carillon.objects.Symbol.APPLY;
import static io.github.erdos.carillon.objects.Symbol.CHARS;
import static io.github.erdos.carillon.objects.Symbol.GLOBE;
import static io.github.erdos.carillon.objects.Symbol.INS;
import static io.github.erdos.carillon.objects.Symbol.LIT;
import static io.github.erdos.carillon.objects.Symbol.NIL;
import static io.github.erdos.carillon.objects.Symbol.O;
import static io.github.erdos.carillon.objects.Symbol.OUTS;
import static io.github.erdos.carillon.objects.Symbol.SCOPE;
import static io.github.erdos.carillon.objects.Symbol.T;

class ExpressionEvaluatorVisitor implements ExpressionVisitor<Expression> {
	private final Primitives primitives = new Primitives();
	private final SpecialForms specialForms = new SpecialForms();

	private final Environment env = new Environment();

	// see: ev function in bel.bel
	public Expression appliedTo(Expression param) {
		return param.apply(this);
	}

	@Override
	public Expression pair(Pair pair) {

		env.whereClear();
		Optional<Variable> maybeVar = Variable.of(pair);
		if (maybeVar.isPresent()) {
			return env.get(maybeVar.get()).orElseThrow(evalException(pair, "Could not resolve variable!"));
		}

		Expression sym = pair.car();

		// TODO: please, consider removing this piece of code.
		if (Symbol.symbol("err") == sym) {
			Expression expression = appliedTo(pair.cadr());

			if (expression != NIL) throw new EvaluationException(expression, "Err called!");

			return expression;
		}

		if (Symbol.LIT.equals(sym)) {
			return specialForms.evalLit(pair);
		}

		if (Symbol.IF.equals(sym)) {
			return specialForms.evalIf(pair, this);
		}

		if (Symbol.QUOTE.equals(sym)) {
			return specialForms.evalQuote(pair);
		}

		if (Symbol.ID.equals(sym)) {
			return primitives.evalId(pair, this);
		}

		if (Symbol.APPLY.equals(sym)) {
			return specialForms.evalApply(pair, this);
		}

		if (Symbol.JOIN.equals(sym)) {
			return primitives.evalJoin(pair, this);
		}

		if (Symbol.CAR.equals(sym)) {
			return primitives.evalCar(pair, env, this);
		}

		if (Symbol.CDR.equals(sym)) {
			return primitives.evalCdr(pair, env, this);
		}

		if (Symbol.TYPE.equals(sym)) {
			return primitives.evalType(pair, this);
		}

		if (Symbol.XAR.equals(sym)) {
			return primitives.evalXar(pair, this);
		}

		if (Symbol.XDR.equals(sym)) {
			return primitives.evaXdr(pair, this);
		}

		if (Symbol.SYM.equals(sym)) {
			return primitives.evalSym(pair, this);
		}

		if (Symbol.NOM.equals(sym)) {
			return primitives.evalNom(pair, this);
		}

		if (Symbol.COIN.equals(sym)) {
			return primitives.coin(pair);
		}

		if (Symbol.SYS.equals(sym)) {
			return primitives.sys(pair, this);
		}

		if (Symbol.SET.equals(sym)) {
			return set(pair);
		}

		if (Symbol.DYN.equals(sym)) {
			return specialForms.evalDyn(pair, env, this);
		}

		if (Symbol.WHERE.equals(sym)) {
			return specialForms.evalWhere(pair, env, this);
		}

		return evalLitCall(pair);
	}

	private Expression evalLitCall(Pair expression) {
		final Expression head = expression.car().apply(this);

		if (!(head instanceof Pair)) {
			throw new EvaluationException(head, "Expected literal expression, instead we got: " + head + " original expression was; " + expression);
		} else if (((Pair) head).cadr() == Symbol.MAC) {
			Pair nestedClo = (Pair) ((Pair) head).caddr(); // lit inside mac!
			Expression macroCallResult = evalFnCallImpl(nestedClo, expression.cdr(), x -> x);
			return this.appliedTo(macroCallResult);
		} else if (((Pair) head).cadr() == Symbol.CLO) {
			return evalFnCallImpl((Pair) head, expression.cdr(), this::appliedTo);
		} else {
			throw new EvaluationException(expression, "We only evaluate MAC or CLO literals!");
		}
	}

	private Expression evalFnCallImpl(Pair fn, Expression passedParamValues, Function<Expression, Expression> argsMapper) {
		assert fn.car() == LIT;
		assert fn.cadr() == Symbol.CLO;

		Expression paramDeclarations = fn.cadddr(); // fourth elem
		Expression body = fn.caddddr(); // fifth elem

		// evaluate arguments if call is not 0-arity.
		final Expression passedEvaledParamValues = passedParamValues == NIL ? NIL : ((Pair) passedParamValues).stream().map(argsMapper).collect(Pair.collect());

		// new scope
		final Map<Variable, Pair> scope = Destructuring
				.destructureArgs(paramDeclarations, passedEvaledParamValues, argsMapper)
				.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, v-> new Pair(v.getKey().getExpression(), v.getValue()))) ;

		if (fn.caddr() != NIL) { // local closure
			((Pair) fn.caddr()).forEach(binding -> {
				Pair bindingPair = (Pair) binding;
				Variable variable = Variable.enforce(bindingPair.car());
				Expression value = bindingPair.cdr();
				scope.putIfAbsent(variable, new Pair(variable.getExpression(), value));
			});
		}

		return env.withLexicals(scope, () -> body.apply(this));
	}

	@Override
	public Expression stream(Stream stream) {
		throw new EvaluationException.FeatureNotImplementedException(stream);
	}

	@Override
	public Expression symbol(Symbol symbol) {
		env.whereClear();
		// lookup order: dynamic, scope, globe, defaults.

		if (symbol == T || symbol == NIL || symbol == O || symbol == APPLY) {
			return symbol;
		}

		Optional<Expression> bound = env.get(Variable.enforce(symbol));

		if (bound.isPresent()) {
			return bound.get();
		} else if (symbol == CHARS) {
			return Constants.CHARS_LIST;
		} else if (symbol == GLOBE) {
			return env.getGlobe();
		} else if (symbol == SCOPE) {
			return env.getScope();
		} else if (symbol == INS) {
			throw new EvaluationException.FeatureNotImplementedException(symbol);
		} else if (symbol == OUTS) {
			throw new EvaluationException.FeatureNotImplementedException(symbol);
		} else {
			throw new UnboundSymbolException(symbol);
		}
	}

	@Override
	public Expression character(Character character) {
		return character;
	}

	Expression set(Pair call) {
		assert Symbol.SET == call.car();

		Expression tail = call.cdr();

		Expression last = NIL;
		while (tail != NIL) {
			Pair pair = (Pair) tail;

			if (pair.car() instanceof Symbol) {
				Symbol key = (Symbol) pair.car();
				Expression value = last = pair.cadr().apply(this);
				env.set(Variable.enforce(key), value);
			} else {
				// key is ignored on purpose. location contains index to it!
				Expression key = pair.car().apply(this);
				Environment.LastLocation location = env.getLastLocation().orElseThrow(() -> new EvaluationException(pair.car(), "Can not find location!"));
				Expression value = last = pair.cadr().apply(this);

				location.update(value);

			}

			tail = ((Pair)((Pair)tail).cdr()).cdr();
		}

		return last;
	}
}