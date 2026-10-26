package dev.joid.lib.font.impl.msdf;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MsdfGlyph {

	private final int        codepoint;
	private final float      advance;
	private final MsdfBounds planeBounds;
	private final MsdfBounds atlasBounds;

	public static @NonNull MsdfGlyph of(final int codepoint, final float advance, final MsdfBounds planeBounds, final MsdfBounds atlasBounds) {
		return new MsdfGlyph(codepoint, advance, planeBounds, atlasBounds);
	}

}