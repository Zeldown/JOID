package dev.joid.lib.shader.pipeline.pass;

import dev.joid.lib.shader.impl.CircleShader;
import dev.joid.lib.shader.impl.ShaderFillType;
import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.ShaderPassContext;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CircleShaderPass implements IShaderPass {

	private final float radius;
	private final float centerX;
	private final float centerY;

	public static @NonNull CircleShaderPass create(final float radius, final float centerX, final float centerY) {
		return new CircleShaderPass(radius, centerX, centerY);
	}

	@Override
	public void bind(final @NonNull ShaderPassContext context) {
		if (!CircleShader.inst().canDraw()) {
			return;
		}

		CircleShader.inst().bind(this.radius, this.centerX, this.centerY, ShaderFillType.TEXTURE);
	}

	@Override
	public void unbind() {
		CircleShader.inst().unbind();
	}

}