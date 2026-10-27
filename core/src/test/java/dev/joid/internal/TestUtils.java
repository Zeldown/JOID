package dev.joid.internal;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.function.BooleanSupplier;

import org.junit.Assert;

import dev.joid.internal.font.DevFont;
import dev.joid.lib.JOID;
import dev.joid.lib.JOIDEnv;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;
import dev.joid.lib.resource.decoder.impl.VideoResourceDecoder;
import dev.joid.lib.utils.thread.ThreadUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestUtils {

	private static MsdfFont montserrat;

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

	public static @NonNull String captureOutChecked(final @NonNull CheckedRunnable runnable) throws Exception {
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

	public static void loadTheDevFont() {
		if (DevFont.MONTSERRAT == null) {
			TestUtils.captureOut(false, DevFont::load);
		}
	}

	public static @NonNull MsdfFont loadMontserrat() {
		if (TestUtils.montserrat == null) {
			TestUtils.captureOut(false, () -> TestUtils.montserrat = MsdfFontLoader.load(JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat/Montserrat-Regular.ttf"), JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat/Montserrat-Bold.ttf")).join());
		}
		return TestUtils.montserrat;
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

	public static boolean awaitBlocked(final @NonNull Thread thread, final @NonNull Object monitor) {
		final long deadline = System.currentTimeMillis() + 30000L;
		while (!TestUtils.isBlockedOn(thread, monitor)) {
			if (System.currentTimeMillis() > deadline) {
				return false;
			}
			Thread.yield();
		}
		return true;
	}

	public static boolean awaitWaiting(final @NonNull Thread thread, final @NonNull Class<?> type, final @NonNull String method) {
		final long deadline = System.currentTimeMillis() + 30000L;
		while (!TestUtils.isWaitingIn(thread, type, method)) {
			if (System.currentTimeMillis() > deadline) {
				return false;
			}
			Thread.yield();
		}
		return true;
	}

	public static void join(final @NonNull Thread thread) {
		try {
			thread.join(30000L);
		} catch (final InterruptedException exception) {
			throw new AssertionError(exception);
		}
		Assert.assertFalse("The thread " + thread.getName() + " did not end in 30 s", thread.isAlive());
	}

	public static void awaitWithin(final @NonNull Runnable wait) {
		final Thread thread = ThreadUtils.daemonThread(wait, "TestUtils/await");
		thread.start();
		TestUtils.join(thread);
	}

	public static void awaitSettled(final @NonNull VideoResourceDecoder decoder) throws InterruptedException {
		TestUtils.await("The video did not settle", decoder::isSettled);
	}

	public static void await(final @NonNull String failure, final @NonNull BooleanSupplier condition) throws InterruptedException {
		final long deadline = System.currentTimeMillis() + 30000L;
		while (!condition.getAsBoolean()) {
			Assert.assertTrue(failure + " in 30 s", System.currentTimeMillis() < deadline);
			Thread.sleep(1L);
		}
	}

	private static boolean isBlockedOn(final Thread thread, final Object monitor) {
		final ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(thread.getId());
		return info != null && info.getThreadState() == Thread.State.BLOCKED && info.getLockInfo() != null && info.getLockInfo().getIdentityHashCode() == System.identityHashCode(monitor);
	}

	private static boolean isWaitingIn(final Thread thread, final Class<?> type, final String method) {
		final ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(thread.getId(), Integer.MAX_VALUE);
		if (info == null || info.getThreadState() != Thread.State.WAITING) {
			return false;
		}

		for (final StackTraceElement element : info.getStackTrace()) {
			if (!element.getClassName().startsWith("java.") && !element.getClassName().startsWith("sun.")) {
				return element.getClassName().equals(type.getName()) && element.getMethodName().equals(method);
			}
		}
		return false;
	}

	@FunctionalInterface
	public interface CheckedRunnable {

		public void run() throws Exception;

	}

}