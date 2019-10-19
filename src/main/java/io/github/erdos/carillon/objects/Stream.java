package io.github.erdos.carillon.objects;

import io.github.erdos.carillon.eval.EvaluationException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import static io.github.erdos.carillon.objects.Symbol.symbol;

public final class Stream implements Expression {

	public static final Stream IN = new Stream(System.in, null);
	public static final Stream OUT = new Stream(null, System.out);

	private boolean open = true;

	private final InputStream inputStream;
	private final OutputStream outputStream;

	private final Queue<Boolean> contents = new LinkedBlockingDeque<>();

	private Stream(InputStream inputStream, OutputStream outputStream) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}

	@Override
	public <T> T apply(ExpressionVisitor<T> visitor) {
		return visitor.stream(this);
	}

	public void close() {
		if (! open) {
			throw new EvaluationException.StreamAlreadyClosedException(this);
		} else {
			open = false;
		}
	}

	void writeOne() {
		contents.offer(true);
	}

	void writeZero() {
		contents.offer(false);
	}

	Expression read() {
		if (!open) {
			return symbol("eof");
		} else if (contents.isEmpty()) {
			return Symbol.NIL;
		} else if (contents.poll()) {
			return Character.character('1');
		} else {
			return Character.character('0');
		}
	}

	Expression stat() {
		if (!open) {
			return symbol("closed");
		} else if (inputStream != null) {
			return symbol("in");
		} else if (outputStream != null) {
			return symbol("out");
		} else {
			throw new IllegalStateException();
		}
	}
}
