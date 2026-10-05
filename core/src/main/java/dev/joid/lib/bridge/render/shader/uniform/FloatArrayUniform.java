package dev.joid.lib.bridge.render.shader.uniform;

import lombok.NonNull;

public interface FloatArrayUniform extends ShaderUniform {

	public void setValue(final @NonNull float[] value);

}