package io.github.erdos.bellang.eval;

import io.github.erdos.bellang.objects.Character;
import io.github.erdos.bellang.objects.Expression;
import io.github.erdos.bellang.objects.Pair;

public class Constants {

	public static final Expression CHARS_LIST;

	static {
		String alphabet = "0123456789,`~@\\()[]; abcdefghijklmnopqrstuvwxzyABCDEFGHIJKLMNOPQRSTUVWXYZ";

		CHARS_LIST = alphabet.chars().mapToObj(c -> Character.character((char) c)).collect(Pair.collect());
	}
}
