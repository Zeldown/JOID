package dev.joid.demo.shader.pipeline.pass;

import dev.joid.demo.shader.impl.DemoInvertShader;
import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.ShaderPassContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DemoInvertShaderPass implements IShaderPass {

	private final float amount;

	@Override
	public void unbind() {
		DemoInvertShader.inst().unbind();
	}

	@Override
	public void bind(final @NonNull ShaderPassContext context) {
		if (!DemoInvertShader.inst().canDraw()) {
			return;
		}

		DemoInvertShader.inst().bind(this.amount);
	}

	@Override
	public int priority() {
		return 200;
	}

}