package dev.joid.lib.font.impl.glyph;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class GlyphLayout<F extends IFontFace> {

	private final List<GlyphPlacement<F>> placements;
	private final double                  width;

	public List<GlyphPlacement<F>> getPlacements() {
		return Collections.unmodifiableList(this.placements);
	}

}