package dev.joid.lib.font.impl.glyph;

import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlyphLayout<F extends IFontFace> {

	private final List<GlyphPlacement<F>> placements;
	private final double                  width;

	public static <F extends IFontFace> @NonNull GlyphLayout<F> of(final @NonNull List<GlyphPlacement<F>> placements, final double width) {
		return new GlyphLayout<>(placements, width);
	}

	public List<GlyphPlacement<F>> getPlacements() {
		return Collections.unmodifiableList(this.placements);
	}

}