package be.zeldown.joid.lib.shader.uniform;

public interface Float4ArrayUniform extends ShaderUniform {

	void setValue(final float[] array, final int program);

}