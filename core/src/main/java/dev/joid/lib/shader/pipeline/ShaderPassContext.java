package dev.joid.lib.shader.pipeline;

import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.utils.geom.BoundingBox;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderPassContext {

	private final double    x;
	private final double    y;
	private final double    width;
	private final double    height;
	private final double    expansion;
	private final PixelGrid grid;
	private final double    regionX;
	private final double    regionY;
	private final double    regionWidth;
	private final double    regionHeight;
	private final int       textureWidth;
	private final int       textureHeight;
	private final int       frameBufferWidth;
	private final int       frameBufferHeight;

	public static @NonNull ShaderPassContext of(final double x, final double y, final double width, final double height, final double expansion, final double margin, final @NonNull PixelGrid grid) {
		final double left = x - expansion;
		final double top = y - expansion;
		final double right = x + width + expansion;
		final double bottom = y + height + expansion;
		final BoundingBox visible = grid.getVisibleArea(margin);
		if (!grid.isAligned()) {
			final int fullWidth = grid.toPixelWidth(right - left);
			final int fullHeight = grid.toPixelHeight(bottom - top);
			final int firstColumn = visible == null ? 0 : ShaderPassContext.firstTexel(left, right, fullWidth, visible.getMinX());
			final int lastColumn = visible == null ? fullWidth : ShaderPassContext.lastTexel(left, right, fullWidth, firstColumn, visible.getMaxX());
			final int firstRow = visible == null ? 0 : ShaderPassContext.firstTexel(top, bottom, fullHeight, visible.getMinY());
			final int lastRow = visible == null ? fullHeight : ShaderPassContext.lastTexel(top, bottom, fullHeight, firstRow, visible.getMaxY());
			final double regionLeft = ShaderPassContext.texelEdge(left, right, fullWidth, firstColumn);
			final double regionTop = ShaderPassContext.texelEdge(top, bottom, fullHeight, firstRow);
			final double regionRight = ShaderPassContext.texelEdge(left, right, fullWidth, lastColumn);
			final double regionBottom = ShaderPassContext.texelEdge(top, bottom, fullHeight, lastRow);
			final int textureWidth = lastColumn - firstColumn;
			final int textureHeight = lastRow - firstRow;
			return new ShaderPassContext(x, y, width, height, expansion, grid, regionLeft, regionTop, regionRight - regionLeft, regionBottom - regionTop, textureWidth, textureHeight, textureWidth, textureHeight);
		}

		final double fullLeft = Math.floor(Math.min(grid.toScreenX(left), grid.toScreenX(right)) + 1E-3D);
		final double fullRight = Math.ceil(Math.max(grid.toScreenX(left), grid.toScreenX(right)) - 1E-3D);
		final double fullTop = Math.floor(Math.min(grid.toScreenY(top), grid.toScreenY(bottom)) + 1E-3D);
		final double fullBottom = Math.ceil(Math.max(grid.toScreenY(top), grid.toScreenY(bottom)) - 1E-3D);
		final double visibleLeft = Math.floor(Math.min(grid.toScreenX(visible.getMinX()), grid.toScreenX(visible.getMaxX())) + 1E-3D);
		final double visibleRight = Math.ceil(Math.max(grid.toScreenX(visible.getMinX()), grid.toScreenX(visible.getMaxX())) - 1E-3D);
		final double visibleTop = Math.floor(Math.min(grid.toScreenY(visible.getMinY()), grid.toScreenY(visible.getMaxY())) + 1E-3D);
		final double visibleBottom = Math.ceil(Math.max(grid.toScreenY(visible.getMinY()), grid.toScreenY(visible.getMaxY())) - 1E-3D);
		final double screenLeft = Math.max(fullLeft, Math.min(visibleLeft, fullRight - 1D));
		final double screenRight = Math.min(fullRight, Math.max(visibleRight, screenLeft + 1D));
		final double screenTop = Math.max(fullTop, Math.min(visibleTop, fullBottom - 1D));
		final double screenBottom = Math.min(fullBottom, Math.max(visibleBottom, screenTop + 1D));
		final double regionLeft = Math.min(grid.fromScreenX(screenLeft), grid.fromScreenX(screenRight));
		final double regionTop = Math.min(grid.fromScreenY(screenTop), grid.fromScreenY(screenBottom));
		final double regionRight = Math.max(grid.fromScreenX(screenLeft), grid.fromScreenX(screenRight));
		final double regionBottom = Math.max(grid.fromScreenY(screenTop), grid.fromScreenY(screenBottom));
		final int textureWidth = Math.max(1, (int) (screenRight - screenLeft));
		final int textureHeight = Math.max(1, (int) (screenBottom - screenTop));
		return new ShaderPassContext(x, y, width, height, expansion, grid, regionLeft, regionTop, regionRight - regionLeft, regionBottom - regionTop, textureWidth, textureHeight, textureWidth, textureHeight);
	}

	public @NonNull ShaderPassContext withFrameBuffer(final int frameBufferWidth, final int frameBufferHeight) {
		return new ShaderPassContext(this.x, this.y, this.width, this.height, this.expansion, this.grid, this.regionX, this.regionY, this.regionWidth, this.regionHeight, this.textureWidth, this.textureHeight, frameBufferWidth, frameBufferHeight);
	}

	public float getTexelWidth() {
		return 1F / this.frameBufferWidth;
	}

	public float getTexelHeight() {
		return 1F / this.frameBufferHeight;
	}

	private static int firstTexel(final double start, final double end, final int count, final double visibleStart) {
		return (int) Math.max(0D, Math.min(count - 1D, Math.floor((visibleStart - start) * count / (end - start))));
	}

	private static int lastTexel(final double start, final double end, final int count, final int first, final double visibleEnd) {
		return (int) Math.min(count, Math.max(first + 1D, Math.ceil((visibleEnd - start) * count / (end - start))));
	}

	private static double texelEdge(final double start, final double end, final int count, final int index) {
		return index == count ? end : start + (end - start) * index / count;
	}

}