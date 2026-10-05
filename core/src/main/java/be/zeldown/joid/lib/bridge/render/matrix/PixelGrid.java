package be.zeldown.joid.lib.bridge.render.matrix;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class PixelGrid {

	private static final double SKEW    = 1E-6D;
	private static final double EPSILON = 1E-3D;

	private final double  unitX;
	private final double  unitY;
	private final double  scaleX;
	private final double  scaleY;
	private final double  originX;
	private final double  originY;
	private final boolean aligned;

	public static @NonNull PixelGrid of(final double scaleX, final double scaleY) {
		return new PixelGrid(scaleX, scaleY, scaleX, scaleY, 0D, 0D, true);
	}

	public static @NonNull PixelGrid of(final @NonNull float[] projection, final @NonNull float[] modelView, final int viewportWidth, final int viewportHeight) {
		final double halfWidth = viewportWidth / 2D;
		final double halfHeight = viewportHeight / 2D;
		final double depth = PixelGrid.product(projection, modelView, 3, 3);
		final double xx = PixelGrid.product(projection, modelView, 0, 0) * halfWidth / depth;
		final double xy = PixelGrid.product(projection, modelView, 1, 0) * halfHeight / depth;
		final double yx = PixelGrid.product(projection, modelView, 0, 1) * halfWidth / depth;
		final double yy = PixelGrid.product(projection, modelView, 1, 1) * halfHeight / depth;
		final double originX = (PixelGrid.product(projection, modelView, 0, 3) / depth + 1D) * halfWidth;
		final double originY = (PixelGrid.product(projection, modelView, 1, 3) / depth + 1D) * halfHeight;
		final double scaleX = Math.hypot(xx, xy);
		final double scaleY = Math.hypot(yx, yy);
		final boolean aligned = Math.abs(xy) <= PixelGrid.SKEW * scaleX && Math.abs(yx) <= PixelGrid.SKEW * scaleY;
		return new PixelGrid(xx, yy, scaleX, scaleY, originX, originY, aligned);
	}

	public double snapX(final double x) {
		return this.aligned ? this.fromScreenX(Math.round(this.toScreenX(x))) : x;
	}

	public double snapY(final double y) {
		return this.aligned ? this.fromScreenY(Math.round(this.toScreenY(y))) : y;
	}

	public double toScreenX(final double x) {
		return this.originX + this.unitX * x;
	}

	public double toScreenY(final double y) {
		return this.originY + this.unitY * y;
	}

	public double fromScreenX(final double screenX) {
		return (screenX - this.originX) / this.unitX;
	}

	public double fromScreenY(final double screenY) {
		return (screenY - this.originY) / this.unitY;
	}

	public int toPixelWidth(final double width) {
		return Math.max(1, (int) Math.ceil(width * this.scaleX - PixelGrid.EPSILON));
	}

	public int toPixelHeight(final double height) {
		return Math.max(1, (int) Math.ceil(height * this.scaleY - PixelGrid.EPSILON));
	}

	private static double product(final float[] projection, final float[] modelView, final int row, final int column) {
		double value = 0D;
		for (int k = 0; k < 4; k++) {
			value += projection[k * 4 + row] * modelView[column * 4 + k];
		}
		return value;
	}

}