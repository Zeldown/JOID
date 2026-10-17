package dev.joid.lib.font.impl.msdf;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MsdfAtlas {

	private final int   width;
	private final int   height;
	private final float size;
	private final float distanceRange;

	public static @NonNull MsdfAtlas of(final int width, final int height, final float size, final float distanceRange) {
		return new MsdfAtlas(width, height, size, distanceRange);
	}

}