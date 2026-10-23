package dev.joid.internal;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestUtils {

	public static @NonNull String captureOut(final @NonNull Runnable runnable) {
		final PrintStream previous = System.out;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setOut(new PrintStream(output, true));
			runnable.run();
		} finally {
			System.setOut(previous);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

	public static @NonNull String captureOut(final boolean devMode, final @NonNull Runnable runnable) {
		final boolean previous = JOIDEnv.isDevMode();
		JOIDEnv.devMode(devMode);
		try {
			return TestUtils.captureOut(runnable);
		} finally {
			JOIDEnv.devMode(previous);
		}
	}

	public static @NonNull String captureErr(final @NonNull Runnable runnable) {
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setErr(new PrintStream(output, true));
			runnable.run();
		} finally {
			System.setErr(previous);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

	public static @NonNull String captureErr(final boolean devMode, final @NonNull Runnable runnable) {
		final boolean previous = JOIDEnv.isDevMode();
		JOIDEnv.devMode(devMode);
		try {
			return TestUtils.captureErr(runnable);
		} finally {
			JOIDEnv.devMode(previous);
		}
	}

	public static void demoMode(final boolean demoMode) {
		try {
			final Field field = JOIDEnv.class.getDeclaredField("demoMode");
			field.setAccessible(true);
			field.setBoolean(null, demoMode);
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	public static Object field(final @NonNull Object target, final @NonNull String name) {
		try {
			final Field field = target.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return field.get(target);
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	public static boolean awaitWaiting(final @NonNull Thread thread) {
		final long deadline = System.currentTimeMillis() + 5000L;
		while (thread.getState() != Thread.State.WAITING) {
			if (System.currentTimeMillis() > deadline) {
				return false;
			}
			Thread.yield();
		}
		return true;
	}

}