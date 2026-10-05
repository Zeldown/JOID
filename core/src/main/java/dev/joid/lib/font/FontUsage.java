package dev.joid.lib.font;

import java.util.Optional;
import java.util.function.DoubleSupplier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FontUsage {

	private static final ThreadLocal<StackTraceElement[]> ORIGIN = new ThreadLocal<>();

	public static double trace(final StackTraceElement[] origin, final @NonNull DoubleSupplier usage) {
		if (origin == null) {
			return usage.getAsDouble();
		}

		final StackTraceElement[] previous = FontUsage.ORIGIN.get();
		FontUsage.ORIGIN.set(origin);
		try {
			return usage.getAsDouble();
		} finally {
			FontUsage.ORIGIN.set(previous);
		}
	}

	public static @NonNull Optional<StackTraceElement[]> getOrigin() {
		return Optional.ofNullable(FontUsage.ORIGIN.get());
	}

}