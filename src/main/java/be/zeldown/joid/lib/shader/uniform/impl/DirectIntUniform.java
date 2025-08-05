package be.zeldown.joid.lib.shader.uniform.impl;

import org.lwjgl.opengl.GL20;

import be.zeldown.joid.lib.shader.uniform.IntUniform;

public class DirectIntUniform extends DirectShaderUniform implements IntUniform {

	public DirectIntUniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final int value) {
		GL20.glUniform1i(this.getLocation(), value);
	}

}