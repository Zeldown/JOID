package dev.joid.internal;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JOIDLog {

	private static final Set<String> KEY_SET = ConcurrentHashMap.newKeySet();

	public static void info(final String message) {
		System.out.println("[JOID] " + message);
	}

	public static void warn(final String message) {
		System.err.println("[JOID] " + message);
	}

	public static void warn(final String message, final Throwable cause) {
		System.err.println("[JOID] " + message);
		cause.printStackTrace();
	}

	public static void warnOnce(final String key, final String message) {
		if (JOIDLog.KEY_SET.add(key)) {
			JOIDLog.warn(message);
		}
	}

	public static void warnOnce(final String key, final String message, final Throwable cause) {
		if (JOIDLog.KEY_SET.add(key)) {
			JOIDLog.warn(message, cause);
		}
	}

	public static void devWarnOnce(final String key, final Supplier<String> message) {
		if (JOIDEnv.isDevMode() && JOIDLog.KEY_SET.add(key)) {
			JOIDLog.warn(message.get());
		}
	}

	public static void devWarnOnce(final String key, final Supplier<String> message, final Throwable cause) {
		if (JOIDEnv.isDevMode() && JOIDLog.KEY_SET.add(key)) {
			JOIDLog.warn(message.get(), cause);
		}
	}

	public static String nameOf(final Class<?> type) {
		return type.getSimpleName().isEmpty() ? type.getName() : type.getSimpleName();
	}

}