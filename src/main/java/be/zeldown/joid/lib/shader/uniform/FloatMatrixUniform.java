package be.zeldown.joid.lib.shader.uniform;

import lombok.NonNull;

public interface FloatMatrixUniform extends ShaderUniform {

	public void setValue(final @NonNull float[] value);

}