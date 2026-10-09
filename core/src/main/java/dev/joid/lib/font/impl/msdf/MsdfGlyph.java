package dev.joid.lib.font.impl.msdf;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MsdfGlyph {

	private final int codepoint;
	private final float advance;
	private final MsdfBounds planeBounds;
	private final MsdfBounds atlasBounds;

}