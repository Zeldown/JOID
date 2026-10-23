package dev.joid.internal;

import java.lang.reflect.Field;
import java.util.Set;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JOIDWarnings {

	public static void forget() {
		try {
			final Field field = JOIDLog.class.getDeclaredField("KEY_SET");
			field.setAccessible(true);
			((Set<?>) field.get(null)).clear();
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

}