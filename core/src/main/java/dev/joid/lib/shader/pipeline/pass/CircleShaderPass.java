package dev.joid.lib.shader.pipeline.pass;

import dev.joid.lib.shader.impl.CircleShader;
import dev.joid.lib.shader.impl.RoundedShaderType;
import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.ShaderPassContext;
import lombok.NonNull;

public class CircleShaderPass implements IShaderPass {

	private final float radius;
	private final float centerX;
	private final float centerY;

	public CircleShaderPass(final float radius, final float centerX, final float centerY) {
		this.radius = radius;
		this.centerX = centerX;
		this.centerY = centerY;
	}

	@Override
	public void unbind() {
		CircleShader.inst().unbind();
	}

	@Override
	public void bind(final @NonNull ShaderPassContext context) {
		if (!CircleShader.inst().canDraw()) {
			return;
		}

		CircleShader.inst().bind(this.radius, this.centerX, this.centerY, RoundedShaderType.TEXTURE);
	}

}