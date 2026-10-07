package dev.joid.impl.lwjgl2.render.shader.uniform;

import org.lwjgl.opengl.GL20;

import dev.joid.impl.lwjgl2.render.shader.Shader;

public final class Float2Uniform extends ShaderUniform implements dev.joid.lib.bridge.render.shader.uniform.Float2Uniform {

	public Float2Uniform(final Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final float f1, final float f2) {
		final int location = super.getLocation();
		super.getShader().queueUniform(location, () -> GL20.glUniform2f(location, f1, f2));
	}

}