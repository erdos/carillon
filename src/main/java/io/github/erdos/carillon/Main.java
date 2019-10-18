package io.github.erdos.carillon;

import io.github.erdos.carillon.eval.EvaluationException;
import io.github.erdos.carillon.eval.RT;
import io.github.erdos.carillon.objects.Expression;
import io.github.erdos.carillon.reader.Reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.util.Arrays;

public class Main {

	public static void main(String[] args) throws IOException {
		Arrays.stream(args).filter(x -> !x.startsWith("-")).forEach(fname -> {
			try {
				runFile(new File(fname));
			} catch (FileNotFoundException e) {
				System.err.println(e.getMessage());
				System.exit(1);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		});

		if (Arrays.stream(args).anyMatch(x -> x.equals("--repl"))) {
			repl();
		}
	}

	private static void runFile(File file) throws IOException {
		try (FileReader fr = new FileReader(file);
		     PushbackReader reader = new PushbackReader(fr)) {
			while (true) {
				Expression expression = Reader.read(reader);

				if (expression == null) return;

				Expression result = RT.eval(expression);
			}
		}
	}

	private static void repl() throws IOException {
		System.out.println("BEL REPL. Press ^D to exit.");
		PushbackReader reader = new PushbackReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("> ");
			System.out.flush();
			Expression expression = Reader.read(reader);

			try {
				Expression result = RT.eval(expression);
				System.out.println(result.toString());

			} catch (EvaluationException e) {
				System.err.println("ERROR.");
				System.err.println(e.getMessage());
			}
		}
	}
}
