package dev.joid.lib.shader.pipeline;

import lombok.NonNull;

public interface IShaderPass {

	public void bind(final @NonNull ShaderPassContext context);
	public void unbind();

	public default float expansion() {
		return 0F;
	}

	public default double reach(final double scale) {
		return this.expansion() * scale + 1D;
	}

}