package be.zeldown.joid.lib.shader.pipeline.dto;

import be.zeldown.joid.lib.bridge.render.matrix.PixelGrid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderPassContext {

	private static final double EPSILON = 1E-3D;

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

	public static @NonNull ShaderPassContext create(final double x, final double y, final double width, final double height, final double expansion, final @NonNull PixelGrid grid) {
		final double left = x - expansion;
		final double top = y - expansion;
		final double right = x + width + expansion;
		final double bottom = y + height + expansion;
		if (!grid.isAligned()) {
			return new ShaderPassContext(x, y, width, height, expansion, grid, left, top, right - left, bottom - top, grid.toPixelWidth(right - left), grid.toPixelHeight(bottom - top));
		}

		final double screenLeft = Math.floor(Math.min(grid.toScreenX(left), grid.toScreenX(right)) + ShaderPassContext.EPSILON);
		final double screenRight = Math.ceil(Math.max(grid.toScreenX(left), grid.toScreenX(right)) - ShaderPassContext.EPSILON);
		final double screenTop = Math.floor(Math.min(grid.toScreenY(top), grid.toScreenY(bottom)) + ShaderPassContext.EPSILON);
		final double screenBottom = Math.ceil(Math.max(grid.toScreenY(top), grid.toScreenY(bottom)) - ShaderPassContext.EPSILON);
		final double regionLeft = Math.min(grid.fromScreenX(screenLeft), grid.fromScreenX(screenRight));
		final double regionTop = Math.min(grid.fromScreenY(screenTop), grid.fromScreenY(screenBottom));
		final double regionRight = Math.max(grid.fromScreenX(screenLeft), grid.fromScreenX(screenRight));
		final double regionBottom = Math.max(grid.fromScreenY(screenTop), grid.fromScreenY(screenBottom));
		return new ShaderPassContext(x, y, width, height, expansion, grid, regionLeft, regionTop, regionRight - regionLeft, regionBottom - regionTop, Math.max(1, (int) (screenRight - screenLeft)), Math.max(1, (int) (screenBottom - screenTop)));
	}

	public float getTexelWidth() {
		return 1F / this.textureWidth;
	}

	public float getTexelHeight() {
		return 1F / this.textureHeight;
	}

}