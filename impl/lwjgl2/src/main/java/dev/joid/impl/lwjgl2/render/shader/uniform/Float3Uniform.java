package dev.joid.impl.lwjgl2.render.shader.uniform;

import org.lwjgl.opengl.GL20;

import dev.joid.impl.lwjgl2.render.shader.Shader;

public final class Float3Uniform extends ShaderUniform implements dev.joid.lib.bridge.render.shader.uniform.Float3Uniform {

	public Float3Uniform(final Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final float f1, final float f2, final float f3) {
		final int location = super.getLocation();
		super.getShader().queueUniform(location, () -> GL20.glUniform3f(location, f1, f2, f3));
	}

}