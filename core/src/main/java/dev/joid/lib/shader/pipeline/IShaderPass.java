package dev.joid.lib.shader.pipeline;

import lombok.NonNull;

public interface IShaderPass {

	public void unbind();
	public void bind(final @NonNull ShaderPassContext context);

	public default float expansion() {
		return 0F;
	}

}