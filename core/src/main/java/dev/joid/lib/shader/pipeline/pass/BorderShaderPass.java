package dev.joid.lib.shader.pipeline.pass;

import dev.joid.lib.color.Color;
import dev.joid.lib.shader.impl.BorderShader;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.ShaderPassContext;
import lombok.NonNull;

public class BorderShaderPass implements IShaderPass {

	private final Color color;
	private final float width;

	private boolean    corners;
	private BorderMode mode;

	protected BorderShaderPass(final @NonNull Color color, final float width) {
		this.color   = color;
		this.width   = width;
		this.corners = true;
		this.mode    = BorderMode.OUT;
	}

	public static @NonNull BorderShaderPass create(final @NonNull Color color, final float width) {
		return new BorderShaderPass(color, width);
	}

	public final <T extends BorderShaderPass> @NonNull T corners(final boolean corners) {
		this.corners = corners;
		return (T) this;
	}

	public final <T extends BorderShaderPass> @NonNull T mode(final @NonNull BorderMode mode) {
		this.mode = mode;
		return (T) this;
	}

	@Override
	public void bind(final @NonNull ShaderPassContext context) {
		if (!BorderShader.inst().canDraw()) {
			return;
		}

		final float x = (float) context.getX();
		final float y = (float) context.getY();
		BorderShader.inst().bind((float) (this.width * context.getGrid().getScaleX()), this.color, context.getTexelWidth(), context.getTexelHeight(), this.corners, this.mode, x, y, x + (float) context.getWidth(), y + (float) context.getHeight());
	}

	@Override
	public void unbind() {
		BorderShader.inst().unbind();
	}

	@Override
	public float expansion() {
		return this.width + 2F;
	}

	@Override
	public double reach(final double scale) {
		return this.width * scale * 2D + 1D;
	}

}