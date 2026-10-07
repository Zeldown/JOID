package dev.joid.lib.shader.pipeline;

import dev.joid.lib.shader.pipeline.dto.ShaderPassContext;
import lombok.NonNull;

public interface ShaderPass {

	public void unbind();

	public int priority();

	public default float expansion() {
		return 0F;
	}

	public void bindForTexture(final @NonNull ShaderPassContext context);

}