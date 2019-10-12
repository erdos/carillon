package io.github.erdos.bellang;

import io.github.erdos.bellang.eval.EvaluationException;
import io.github.erdos.bellang.eval.RT;
import io.github.erdos.bellang.objects.Expression;
import io.github.erdos.bellang.reader.Reader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;

public class Main {

    public static void main(String[] args) throws IOException {

        System.out.println("BREPL.");
        PushbackReader reader = new PushbackReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            System.out.flush();
            Expression expression = Reader.read(reader);

            try {
                Expression result = RT.eval(expression);
                System.out.println(result.toString());

            } catch (EvaluationException e) {
                System.err.println("ERROR " + e.getMessage());
            }
        }
    }
}
