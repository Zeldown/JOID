package dev.joid.internal;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.rules.ExternalResource;

import lombok.NonNull;

public final class ConsoleCapture extends ExternalResource {

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private final ByteArrayOutputStream err = new ByteArrayOutputStream();

	private PrintStream previousOut;
	private PrintStream previousErr;

	@Override
	protected void before() {
		this.previousOut = System.out;
		this.previousErr = System.err;
		System.setOut(new PrintStream(this.out, true));
		System.setErr(new PrintStream(this.err, true));
	}

	@Override
	protected void after() {
		System.setOut(this.previousOut);
		System.setErr(this.previousErr);
	}

	public @NonNull String getOut() {
		return new String(this.out.toByteArray(), StandardCharsets.UTF_8);
	}

	public @NonNull String getErr() {
		return new String(this.err.toByteArray(), StandardCharsets.UTF_8);
	}

}