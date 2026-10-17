package dev.joid.lib.font.impl.msdf;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MsdfMetrics {

	private final float lineHeight;
	private final float ascender;
	private final float descender;
	private final float underlineY;
	private final float underlineThickness;

	public static @NonNull MsdfMetrics of(final float lineHeight, final float ascender, final float descender, final float underlineY, final float underlineThickness) {
		return new MsdfMetrics(lineHeight, ascender, descender, underlineY, underlineThickness);
	}

}