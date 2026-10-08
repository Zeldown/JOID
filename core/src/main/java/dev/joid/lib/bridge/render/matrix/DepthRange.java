package dev.joid.lib.bridge.render.matrix;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DepthRange {

	public static @NonNull float[] toZeroToOne(final @NonNull float[] projection) {
		if (projection.length != 16) {
			throw new IllegalArgumentException("A projection matrix holds 16 floats, got " + projection.length);
		}

		final float[] matrix = projection.clone();
		for (int column = 0; column < 4; column++) {
			matrix[column * 4 + 2] = 0.5F * projection[column * 4 + 2] + 0.5F * projection[column * 4 + 3];
		}
		return matrix;
	}

}