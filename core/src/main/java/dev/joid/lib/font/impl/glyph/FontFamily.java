package dev.joid.lib.font.impl.glyph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.font.FontWeight;
import lombok.NonNull;

public final class FontFamily<F extends IFontFace> {

	private final List<F> faces;

	private FontFamily(final List<F> faces) {
		this.faces = faces;
	}

	@SafeVarargs
	public static <F extends IFontFace> @NonNull FontFamily<F> of(final @NonNull F @NonNull... faces) {
		if (faces.length == 0) {
			throw new IllegalArgumentException("A font family needs at least one face, got none: pass its faces to FontFamily.of(...)");
		}

		final List<F> sorted = new ArrayList<>();
		for (final F face : faces) {
			for (final F other : sorted) {
				if (other.getWeight() == face.getWeight() && other.isItalic() == face.isItalic()) {
					throw new IllegalArgumentException("Two faces share the weight " + face.getWeight().getValue() + (face.isItalic() ? " italic" : "") + ", keep one face per weight and style");
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
		if (face.getWeight() != weight) {
			this.warn(weight, italic, face);
		}
		return face;
	}

	public final @NonNull List<F> getFaces() {
		return Collections.unmodifiableList(this.faces);
	}

	private void warn(final @NonNull FontWeight weight, final boolean italic, final @NonNull F face) {
		JOIDLog.devWarnOnce("FontFamily." + System.identityHashCode(this) + "." + FontFamily.style(weight, italic), () -> "The font weight " + FontFamily.style(weight, italic) + " is not loaded in the family of " + face.getName() + ", " + FontFamily.style(face.getWeight(), face.isItalic()) + " is drawn instead (loaded: " + this.faces.stream().map(FontFamily::describe).distinct().collect(Collectors.joining(", ")) + ")");
	}

	private static @NonNull String style(final @NonNull FontWeight weight, final boolean italic) {
		return weight.getValue() + (italic ? " italic" : "");
	}

	private static @NonNull String describe(final @NonNull IFontFace face) {
		return FontFamily.style(face.getWeight(), face.isItalic()) + " " + face.getName();
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