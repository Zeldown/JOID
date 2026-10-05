package be.zeldown.joid.lib.shader.pipeline.dto;

import be.zeldown.joid.lib.bridge.render.matrix.PixelScale;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderPassContext {

	private final double     x;
	private final double     y;
	private final double     width;
	private final double     height;
	private final double     expansion;
	private final PixelScale scale;
	private final int        textureWidth;
	private final int        textureHeight;

	public static @NonNull ShaderPassContext create(final double x, final double y, final double width, final double height, final double expansion, final @NonNull PixelScale scale) {
		return new ShaderPassContext(x, y, width, height, expansion, scale, scale.toPixelWidth(width + expansion * 2D), scale.toPixelHeight(height + expansion * 2D));
	}

	public float getTexelWidth() {
		return 1F / this.textureWidth;
	}

	public float getTexelHeight() {
		return 1F / this.textureHeight;
	}

}