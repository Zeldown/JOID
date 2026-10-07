package dev.joid.impl.lwjgl2.render.shader.uniform;

import org.lwjgl.opengl.GL20;

import dev.joid.impl.lwjgl2.render.shader.Shader;

public final class FloatUniform extends ShaderUniform implements dev.joid.lib.bridge.render.shader.uniform.FloatUniform {

	public FloatUniform(final Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final float value) {
		final int location = super.getLocation();
		super.getShader().queueUniform(location, () -> GL20.glUniform1f(location, value));
	}

}