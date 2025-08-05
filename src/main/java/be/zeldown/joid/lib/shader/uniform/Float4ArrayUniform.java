package be.zeldown.joid.lib.shader.uniform;

import lombok.NonNull;

public interface Float4ArrayUniform extends ShaderUniform {

	public void setValue(final @NonNull float[] array, final int program);

}