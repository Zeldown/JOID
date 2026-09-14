package be.zeldown.joid.impl.lwjgl3.render.shader.uniform;

import org.lwjgl.opengl.GL20C;

import be.zeldown.joid.impl.lwjgl3.render.shader.Shader;

public final class Float2Uniform extends ShaderUniform implements be.zeldown.joid.lib.bridge.render.shader.uniform.Float2Uniform {

	public Float2Uniform(final Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final float f1, final float f2) {
		final int location = super.getLocation();
		super.getShader().queueUniform(location, () -> GL20C.glUniform2f(location, f1, f2));
	}

}