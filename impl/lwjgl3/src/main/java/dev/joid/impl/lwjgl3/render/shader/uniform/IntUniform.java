package dev.joid.impl.lwjgl3.render.shader.uniform;

import org.lwjgl.opengl.GL20C;

import dev.joid.impl.lwjgl3.render.shader.Shader;

public final class IntUniform extends ShaderUniform implements dev.joid.lib.bridge.render.shader.uniform.IntUniform {

	public IntUniform(final Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final int value) {
		final int location = super.getLocation();
		super.getShader().queueUniform(location, () -> GL20C.glUniform1i(location, value));
	}

}