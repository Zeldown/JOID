package be.zeldown.joid.lib.shader.uniform.impl;

import org.lwjgl.opengl.GL20;

import be.zeldown.joid.lib.shader.uniform.FloatUniform;

public class DirectFloatUniform extends DirectShaderUniform implements FloatUniform {

	public DirectFloatUniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final float value) {
		GL20.glUniform1f(this.getLocation(), value);
	}

}