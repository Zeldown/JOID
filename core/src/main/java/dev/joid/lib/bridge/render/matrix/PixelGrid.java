package dev.joid.lib.bridge.render.matrix;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class PixelGrid {

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
		final boolean flat = Math.abs(PixelGrid.product(projection, modelView, 3, 0)) <= 1E-9D * Math.abs(depth) && Math.abs(PixelGrid.product(projection, modelView, 3, 1)) <= 1E-9D * Math.abs(depth);
		final boolean aligned = flat && xx != 0D && yy != 0D && Math.abs(xy) <= 1E-6D * scaleX && Math.abs(yx) <= 1E-6D * scaleY;
		return new PixelGrid(xx, yy, scaleX, scaleY, originX, originY, aligned);
	}

	public double snapX(final double x) {
		return this.aligned ? this.fromScreenX(PixelGrid.round(this.toScreenX(x))) : x;
	}

	public double snapY(final double y) {
		return this.aligned ? this.fromScreenY(PixelGrid.round(this.toScreenY(y))) : y;
	}

	public double snapRight(final double left, final double right) {
		final double snapped = this.snapX(right);
		if (!this.aligned || left == right || snapped != this.snapX(left)) {
			return snapped;
		}

		return this.fromScreenX(PixelGrid.round(this.toScreenX(left)) + Math.signum((right - left) * this.unitX));
	}

	public double snapBottom(final double top, final double bottom) {
		final double snapped = this.snapY(bottom);
		if (!this.aligned || top == bottom || snapped != this.snapY(top)) {
			return snapped;
		}

		return this.fromScreenY(PixelGrid.round(this.toScreenY(top)) + Math.signum((bottom - top) * this.unitY));
	}

	public double snapWidth(final double left, final double width) {
		if (!this.aligned || width == 0D) {
			return left + width;
		}

		return this.fromScreenX(PixelGrid.round(this.toScreenX(left)) + Math.signum(width * this.unitX) * Math.max(1D, PixelGrid.round(Math.abs(width * this.unitX))));
	}

	public double snapHeight(final double top, final double height) {
		if (!this.aligned || height == 0D) {
			return top + height;
		}

		return this.fromScreenY(PixelGrid.round(this.toScreenY(top)) + Math.signum(height * this.unitY) * Math.max(1D, PixelGrid.round(Math.abs(height * this.unitY))));
	}

	public @NonNull Span spanX(final double x, final double width) {
		if (!this.aligned || width == 0D) {
			return new Span(x, x + width, 1F);
		}

		final Span pixels = PixelGrid.span(this.toScreenX(x), this.toScreenX(x + width));
		return new Span(this.fromScreenX(pixels.start), this.fromScreenX(pixels.end), pixels.coverage);
	}

	public @NonNull Span spanY(final double y, final double height) {
		if (!this.aligned || height == 0D) {
			return new Span(y, y + height, 1F);
		}

		final Span pixels = PixelGrid.span(this.toScreenY(y), this.toScreenY(y + height));
		return new Span(this.fromScreenY(pixels.start), this.fromScreenY(pixels.end), pixels.coverage);
	}

	public double quantizeX(final double offset) {
		return this.aligned ? PixelGrid.round(offset * this.unitX) / this.unitX : offset;
	}

	public double quantizeY(final double offset) {
		return this.aligned ? PixelGrid.round(offset * this.unitY) / this.unitY : offset;
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
		return Math.max(1, (int) Math.ceil(width * this.scaleX - 1E-3D));
	}

	public int toPixelHeight(final double height) {
		return Math.max(1, (int) Math.ceil(height * this.scaleY - 1E-3D));
	}

	private static double round(final double value) {
		return Math.floor(value + 0.5D + 1E-6D);
	}

	private static Span span(final double start, final double end) {
		final double extent = Math.abs(end - start);
		if (extent >= 3D) {
			return new Span(PixelGrid.round(start), PixelGrid.round(end), 1F);
		}

		final double direction = Math.signum(end - start);
		final double pixels = Math.max(1D, PixelGrid.round(extent));
		final double first = PixelGrid.round((start + end - direction * pixels) / 2D);
		return new Span(first, first + direction * pixels, (float) Math.min(1D, extent));
	}

	private static double product(final float[] projection, final float[] modelView, final int row, final int column) {
		double value = 0D;
		for (int k = 0; k < 4; k++) {
			value += projection[k * 4 + row] * modelView[column * 4 + k];
		}
		return value;
	}

	@Getter
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class Span {

		private final double start;
		private final double end;
		private final float  coverage;

	}

}