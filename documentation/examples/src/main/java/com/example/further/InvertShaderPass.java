package com.example.further;

import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.ShaderPassContext;

// #region pass
public class InvertShaderPass implements IShaderPass {

	private final float amount;

	protected InvertShaderPass(final float amount) {
		this.amount = amount;
	}

	public static InvertShaderPass create(final float amount) {
		return new InvertShaderPass(amount);
	}

	@Override
	public void bind(final ShaderPassContext context) {
		if (InvertShader.inst().canDraw()) {
			InvertShader.inst().bind(this.amount);
		}
	}

	@Override
	public void unbind() {
		InvertShader.inst().unbind();
	}

}
// #endregion