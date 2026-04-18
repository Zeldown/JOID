package be.zeldown.joid.lib.shader.uniform.impl;

import org.lwjgl.opengl.GL20;

import be.zeldown.joid.lib.shader.uniform.BooleanUniform;

public class DirectBooleanUniform extends DirectShaderUniform implements BooleanUniform {

	public DirectBooleanUniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final boolean value) {
		GL20.glUniform1i(this.getLocation(), value ? 1 : 0);
	}

}