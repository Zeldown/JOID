package be.zeldown.joid.lib.font.impl.glyph.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class GlyphLayout<F extends IGlyphFace> {

	private final List<GlyphPlacement<F>> placements;
	private final double                  width;

}