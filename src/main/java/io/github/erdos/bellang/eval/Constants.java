package io.github.erdos.bellang.eval;

import io.github.erdos.bellang.objects.Expression;
import io.github.erdos.bellang.objects.Pair;

import java.util.ArrayList;
import java.util.List;

public class Constants {

	public static final Expression CHARS_LIST;

	static {
		List<Pair> pairs = new ArrayList<>();

		String alphabet = "0123456789,`~@\\()[]; abcdefghijklmnopqrstuvwxzyABCDEFGHIJKLMNOPQRSTUVWXYZ";

		alphabet.chars().forEach(c -> {
			// TODO: build list
		});

		CHARS_LIST = RT.list(pairs);


	}
}
