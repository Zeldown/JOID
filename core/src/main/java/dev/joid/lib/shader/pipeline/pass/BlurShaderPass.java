package dev.joid.lib.shader.pipeline.pass;

import dev.joid.lib.shader.impl.BlurShader;
import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.ShaderPassContext;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class BlurShaderPass implements IShaderPass {

	private final float radius;
	private final boolean horizontal;

	public static @NonNull BlurShaderPass create(final float radius, final boolean horizontal) {
		return new BlurShaderPass(radius, horizontal);
	}

	@Override
	public void bind(final @NonNull ShaderPassContext context) {
		if (!BlurShader.inst().canDraw()) {
			return;
		}

		final double scale = this.horizontal ? context.getGrid().getScaleX() : context.getGrid().getScaleY();
		BlurShader.inst().bind((float) (this.radius * scale), this.horizontal ? 1F : 0F, this.horizontal ? 0F : 1F, context.getTexelWidth(), context.getTexelHeight());
	}

	@Override
	public void unbind() {
		BlurShader.inst().unbind();
	}

	@Override
	public float expansion() {
		return this.radius;
	}

	@Override
	public double reach(final double scale) {
		return Math.max(this.radius * scale, 32D) + 1D;
	}

}