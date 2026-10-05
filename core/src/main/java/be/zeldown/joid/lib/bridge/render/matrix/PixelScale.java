package be.zeldown.joid.lib.bridge.render.matrix;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class PixelScale {

	private static final double EPSILON = 1E-3D;

	private final double x;
	private final double y;

	public static @NonNull PixelScale of(final double x, final double y) {
		return new PixelScale(x, y);
	}

	public static @NonNull PixelScale of(final @NonNull float[] projection, final @NonNull float[] modelView, final int viewportWidth, final int viewportHeight) {
		final double halfWidth = viewportWidth / 2D;
		final double halfHeight = viewportHeight / 2D;
		final double depth = Math.abs(PixelScale.product(projection, modelView, 3, 3));
		final double x = Math.hypot(PixelScale.product(projection, modelView, 0, 0) * halfWidth, PixelScale.product(projection, modelView, 1, 0) * halfHeight);
		final double y = Math.hypot(PixelScale.product(projection, modelView, 0, 1) * halfWidth, PixelScale.product(projection, modelView, 1, 1) * halfHeight);
		return new PixelScale(x / depth, y / depth);
	}

	public int toPixelWidth(final double width) {
		return Math.max(1, (int) Math.ceil(width * this.x - PixelScale.EPSILON));
	}

	public int toPixelHeight(final double height) {
		return Math.max(1, (int) Math.ceil(height * this.y - PixelScale.EPSILON));
	}

	private static double product(final float[] projection, final float[] modelView, final int row, final int column) {
		double value = 0D;
		for (int k = 0; k < 4; k++) {
			value += projection[k * 4 + row] * modelView[column * 4 + k];
		}
		return value;
	}

}