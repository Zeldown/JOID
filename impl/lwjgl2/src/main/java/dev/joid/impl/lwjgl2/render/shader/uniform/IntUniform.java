package dev.joid.impl.lwjgl2.render.shader.uniform;

import org.lwjgl.opengl.GL20;


public final class IntUniform extends ShaderUniform implements dev.joid.lib.bridge.render.shader.uniform.IntUniform {

	public IntUniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final int value) {
		GL20.glUniform1i(super.getLocation(), value);
	}

}