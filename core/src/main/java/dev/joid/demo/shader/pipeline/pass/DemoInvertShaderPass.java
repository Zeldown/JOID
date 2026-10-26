package dev.joid.demo.shader.pipeline.pass;

import dev.joid.demo.shader.impl.DemoInvertShader;
import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.ShaderPassContext;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoInvertShaderPass implements IShaderPass {

	private final float amount;

	public static @NonNull DemoInvertShaderPass create(final float amount) {
		return new DemoInvertShaderPass(amount);
	}

	@Override
	public void bind(final @NonNull ShaderPassContext context) {
		if (!DemoInvertShader.inst().canDraw()) {
			return;
		}

		DemoInvertShader.inst().bind(this.amount);
	}

	@Override
	public void unbind() {
		DemoInvertShader.inst().unbind();
	}

}