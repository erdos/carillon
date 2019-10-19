package io.github.erdos.carillon.eval;

import io.github.erdos.carillon.eval.Environment.LastLocation;
import io.github.erdos.carillon.objects.Expression;
import io.github.erdos.carillon.objects.Pair;
import io.github.erdos.carillon.objects.Symbol;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.erdos.carillon.objects.Symbol.NIL;
import static io.github.erdos.carillon.objects.Symbol.symbol;

public class SpecialForms {
	public Expression evalIf(Pair cond, ExpressionEvaluatorVisitor visitor) {
		assert Symbol.IF.equals(cond.car());

		cond = (Pair) cond.cdr();

		while (cond != null) {
			Expression evaled = visitor.appliedTo(cond.car());

			if (evaled != NIL) {
				return visitor.appliedTo(cond.cadr());
			} else if (((Pair) cond.cdr()).cdr() == NIL) {
				break;
			} else if (   ((Pair)((Pair) cond.cdr()).cdr()).cdr() == NIL) {
				return visitor.appliedTo(cond.caddr());
			} else {
				cond = (Pair) ((Pair) cond.cdr()).cdr();
			}
		}

		return NIL;
	}

	public Expression evalQuote(Pair quoted) {
		assert Symbol.QUOTE.equals(quoted.car());
		return quoted.cadr();
	}

	public Expression evalLit(Pair lit) {
		assert Symbol.LIT.equals(lit.car());
		return lit;
	}

	public Expression evalApply(Pair lit, ExpressionEvaluatorVisitor visitor) {
		assert Symbol.APPLY.equals(lit.car());
		// TODO: this is really not effective.

		List<Expression> items = ((Pair) lit.cdr()).stream().map(visitor::appliedTo).collect(Collectors.toList());

		if (items.size() > 1) {
			Expression last = items.get(items.size() - 1);

			items.remove(items.size() - 1);

			if (last instanceof Pair) {
				((Pair) last).forEach(items::add);
			} else {
				assert last == NIL;
			}
		}

		return items.stream().map(RT::quote).collect(Pair.collect()).apply(visitor);
	}

	public Expression evalDyn(Pair lit, Environment env, ExpressionEvaluatorVisitor evaluator) {
		Variable variable = Variable.enforce(lit.nthOrNil(1));
		Expression value = evaluator.appliedTo(lit.nth(2));
		Expression body = lit.nth(3);

		return env.withDynamicBinding(variable, value, () -> evaluator.appliedTo(body));
	}

	/**
	 * Evaluates x. If its value comes from a pair, returns a list of that
	 * pair and either a or d depending on whether the value is stored in
	 * the car or cdr. Signals an error if the value of x doesn't come from
	 * a pair.
	 */
	public Expression evalWhere(Pair x, Environment env, ExpressionEvaluatorVisitor evaluator) {
		Expression param = x.nthOrNil(1);

		Expression value = evaluator.appliedTo(param);

		Optional<LastLocation> location = env.getLastLocation();

		if (location.isPresent()) {
			Pair parent = location.get().pair;
			Symbol loc = location.get().car ? symbol("a") : symbol("d");
			return RT.list(parent, loc);
		} else {
			throw new EvaluationException(value, "Value does not come from a pair!");
		}
	}

	// where -- wtf
	// after -- sorrendiseg megtartasa
	// ccc -- wtf
	// thread -- wtf

}
