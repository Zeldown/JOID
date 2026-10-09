package dev.joid.lib.shader.pipeline;

import dev.joid.lib.shader.pipeline.dto.ShaderPassContext;
import lombok.NonNull;

public interface IShaderPass {

	public void unbind();
	public void bind(final @NonNull ShaderPassContext context);

	public int priority();

	public default float expansion() {
		return 0F;
	}

}