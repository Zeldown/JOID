package dev.joid.lib.shader.pipeline.pass;

import dev.joid.lib.shader.impl.BlurShader;
import dev.joid.lib.shader.pipeline.ShaderPass;
import dev.joid.lib.shader.pipeline.dto.ShaderPassContext;
import lombok.NonNull;

public class BlurShaderPass implements ShaderPass {

	private final float radius;
	private final int priorityValue;
	private final boolean horizontal;

	public BlurShaderPass(final float radius, final boolean horizontal, final int iteration) {
		this.radius = radius;
		this.horizontal = horizontal;
		this.priorityValue = 150 + iteration * 2 + (horizontal ? 0 : 1);
	}

	@Override
	public void unbind() {
		BlurShader.inst().unbind();
	}

	@Override
	public int priority() {
		return this.priorityValue;
	}

	@Override
	public float expansion() {
		return this.radius;
	}

	@Override
	public void bindForTexture(final @NonNull ShaderPassContext context) {
		if (!BlurShader.inst().isAvailable()) {
			return;
		}

		final double scale = this.horizontal ? context.getGrid().getScaleX() : context.getGrid().getScaleY();
		BlurShader.inst().bind((float) (this.radius * scale), this.horizontal ? 1F : 0F, this.horizontal ? 0F : 1F, context.getTexelWidth(), context.getTexelHeight());
	}

}