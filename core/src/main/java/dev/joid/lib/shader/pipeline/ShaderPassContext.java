package dev.joid.lib.shader.pipeline;

import dev.joid.lib.bridge.render.matrix.PixelGrid;
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

	public static @NonNull ShaderPassContext create(final double x, final double y, final double width, final double height, final double expansion, final @NonNull PixelGrid grid) {
		final double left = x - expansion;
		final double top = y - expansion;
		final double right = x + width + expansion;
		final double bottom = y + height + expansion;
		if (!grid.isAligned()) {
			final int textureWidth = grid.toPixelWidth(right - left);
			final int textureHeight = grid.toPixelHeight(bottom - top);
			return new ShaderPassContext(x, y, width, height, expansion, grid, left, top, right - left, bottom - top, textureWidth, textureHeight, textureWidth, textureHeight);
		}

		final double screenLeft = Math.floor(Math.min(grid.toScreenX(left), grid.toScreenX(right)) + 1E-3D);
		final double screenRight = Math.ceil(Math.max(grid.toScreenX(left), grid.toScreenX(right)) - 1E-3D);
		final double screenTop = Math.floor(Math.min(grid.toScreenY(top), grid.toScreenY(bottom)) + 1E-3D);
		final double screenBottom = Math.ceil(Math.max(grid.toScreenY(top), grid.toScreenY(bottom)) - 1E-3D);
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

}