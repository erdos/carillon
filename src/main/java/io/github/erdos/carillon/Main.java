package io.github.erdos.carillon;

import io.github.erdos.carillon.eval.EvaluationException;
import io.github.erdos.carillon.eval.RT;
import io.github.erdos.carillon.objects.Expression;
import io.github.erdos.carillon.reader.Reader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException {

        // TODO: iterate over files to eval them.

        if (Arrays.stream(args).anyMatch(x->x.equals("--repl"))) {
            repl();
        }

        repl();
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
