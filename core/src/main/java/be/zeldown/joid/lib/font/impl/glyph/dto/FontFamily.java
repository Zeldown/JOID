package be.zeldown.joid.lib.font.impl.glyph.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import be.zeldown.joid.lib.font.FontWeight;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class FontFamily<F extends IGlyphFace> {

	private static final int NORMAL = 400;
	private static final int MEDIUM = 500;

	private final List<F> faces;

	private FontFamily(final List<F> faces) {
		this.faces = faces;
	}

	@SafeVarargs
	public static <F extends IGlyphFace> @NonNull FontFamily<F> of(final @NonNull F @NonNull... faces) {
		if (faces.length == 0) {
			throw new IllegalArgumentException("A font family needs at least one face");
		}

		final List<F> sorted = new ArrayList<>();
		for (final F face : faces) {
			for (final F other : sorted) {
				if (other.getWeight() == face.getWeight() && other.isItalic() == face.isItalic()) {
					throw new IllegalArgumentException("Two faces share the weight " + face.getWeight() + (face.isItalic() ? " italic" : ""));
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

		return FontFamily.match(styled.isEmpty() ? this.faces : styled, weight.getValue());
	}

	private static <F extends IGlyphFace> @NonNull F match(final @NonNull List<F> faces, final int desired) {
		F lighter = null;
		F heavier = null;
		F within = null;
		for (final F face : faces) {
			final int weight = face.getWeight().getValue();
			if (weight == desired) {
				return face;
			}

			if (weight < desired && (lighter == null || weight > lighter.getWeight().getValue())) {
				lighter = face;
			}

			if (weight > desired && (heavier == null || weight < heavier.getWeight().getValue())) {
				heavier = face;
			}

			if (weight > desired && weight <= FontFamily.MEDIUM && (within == null || weight < within.getWeight().getValue())) {
				within = face;
			}
		}

		if (desired >= FontFamily.NORMAL && desired <= FontFamily.MEDIUM) {
			return within != null ? within : lighter != null ? lighter : heavier;
		}

		if (desired < FontFamily.NORMAL) {
			return lighter != null ? lighter : heavier;
		}

		return heavier != null ? heavier : lighter;
	}

}