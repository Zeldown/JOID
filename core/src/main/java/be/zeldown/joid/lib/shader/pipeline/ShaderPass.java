package be.zeldown.joid.lib.shader.pipeline;

import be.zeldown.joid.lib.shader.pipeline.dto.ShaderPassContext;
import lombok.NonNull;

public interface ShaderPass {

	public void unbind();

	public int priority();

	public default float expansion() {
		return 0F;
	}

	public default boolean supportsDirectBind() {
		return false;
	}

	public void bindDirect(final @NonNull ShaderPassContext context);

	public void bindForTexture(final @NonNull ShaderPassContext context);

}