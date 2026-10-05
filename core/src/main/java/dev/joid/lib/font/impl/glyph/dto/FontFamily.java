package dev.joid.lib.font.impl.glyph.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import dev.joid.internal.JOID;
import dev.joid.lib.font.FontUsage;
import dev.joid.lib.font.FontWeight;
import lombok.Getter;
import lombok.NonNull;

public final class FontFamily<F extends IFontFace> {

	@Getter private final List<F> faces;
	private final Set<FontWeight> warned = ConcurrentHashMap.newKeySet();

	private FontFamily(final List<F> faces) {
		this.faces = faces;
	}

	@SafeVarargs
	public static <F extends IFontFace> @NonNull FontFamily<F> of(final @NonNull F @NonNull... faces) {
		if (faces.length == 0) {
			throw new IllegalArgumentException("A font family needs at least one face");
		}

		final List<F> sorted = new ArrayList<>();
		for (final F face : faces) {
			for (final F other : sorted) {
				if (other.getWeight() == face.getWeight() && other.isItalic() == face.isItalic()) {
					throw new IllegalArgumentException("Two faces share the weight " + face.getWeight().getValue() + (face.isItalic() ? " italic" : ""));
				}
			}
			sorted.add(face);
		}

		sorted.sort(Comparator.comparingInt(face -> face.getWeight().getValue()));
		return new FontFamily<>(Collections.unmodifiableList(sorted));
	}

	public @NonNull F resolve(final @NonNull FontWeight weight, final boolean italic) {
		final List<F> styled = new ArrayList<>();
		for (final F face : this.faces) {
			if (face.isItalic() == italic) {
				styled.add(face);
			}
		}

		final F face = FontFamily.match(styled.isEmpty() ? this.faces : styled, weight.getValue());
		if (face.getWeight() != weight && JOID.inst().isDevMode() && this.warned.add(weight)) {
			this.warn(weight, face);
		}
		return face;
	}

	private void warn(final @NonNull FontWeight weight, final @NonNull F face) {
		final StringBuilder warning = new StringBuilder("[JOID] The font weight ").append(weight.getValue()).append(" is not loaded in the family of ").append(face.getName());
		warning.append(", ").append(face.getWeight().getValue()).append(" is drawn instead (loaded: ").append(this.faces.stream().map(FontFamily::describe).distinct().collect(Collectors.joining(", "))).append(")");

		for (final StackTraceElement element : FontUsage.getOrigin().orElseGet(FontFamily::locate)) {
			warning.append(System.lineSeparator()).append("\tat ").append(element);
		}
		System.err.println(warning);
	}

	private static @NonNull StackTraceElement[] locate() {
		final StackTraceElement[] trace = new Throwable().getStackTrace();
		int start = 0;
		while (start < trace.length && (trace[start].getClassName().equals(FontFamily.class.getName()) || trace[start].getClassName().startsWith("java."))) {
			start++;
		}
		return Arrays.copyOfRange(trace, start, trace.length);
	}

	private static @NonNull String describe(final @NonNull IFontFace face) {
		return face.getWeight().getValue() + " " + face.getName();
	}

	private static boolean closer(final int candidate, final int current, final int desired) {
		final int distance = Math.abs(candidate - desired);
		final int best = Math.abs(current - desired);
		if (distance != best) {
			return distance < best;
		}

		return desired == 400 || desired > 500 ? candidate > current : candidate < current;
	}

	private static <F extends IFontFace> @NonNull F match(final @NonNull List<F> faces, final int desired) {
		F match = faces.get(0);
		for (final F face : faces) {
			if (FontFamily.closer(face.getWeight().getValue(), match.getWeight().getValue(), desired)) {
				match = face;
			}
		}
		return match;
	}

}