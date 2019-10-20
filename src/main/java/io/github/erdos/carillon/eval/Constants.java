package io.github.erdos.carillon.eval;

import io.github.erdos.carillon.objects.Character;
import io.github.erdos.carillon.objects.Expression;
import io.github.erdos.carillon.objects.Pair;

import java.util.stream.IntStream;

import static io.github.erdos.carillon.eval.RT.pair;

public class Constants {

	public static final Expression CHARS_LIST;

	static {
		CHARS_LIST = IntStream.range(-1, 128).mapToObj(c ->
			pair(Character.character((char) c),
					Integer.toBinaryString(c).chars().mapToObj(x -> Character.character((char) x))
							.collect(Pair.collect()))).collect(Pair.collect());
	}
}
