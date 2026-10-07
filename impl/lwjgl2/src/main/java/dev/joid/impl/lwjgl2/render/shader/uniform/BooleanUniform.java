package dev.joid.impl.lwjgl2.render.shader.uniform;

import org.lwjgl.opengl.GL20;

import dev.joid.impl.lwjgl2.render.shader.Shader;

public final class BooleanUniform extends ShaderUniform implements dev.joid.lib.bridge.render.shader.uniform.BooleanUniform {

	public BooleanUniform(final Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final boolean value) {
		final int location = super.getLocation();
		super.getShader().queueUniform(location, () -> GL20.glUniform1i(location, value ? 1 : 0));
	}

}