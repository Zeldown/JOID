package be.zeldown.joid.lib.shader.uniform.impl;

import org.lwjgl.opengl.GL20;

import be.zeldown.joid.lib.shader.uniform.Float2Uniform;

public class DirectFloat2Uniform extends DirectShaderUniform implements Float2Uniform {

	public DirectFloat2Uniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final float f1, final float f2) {
		GL20.glUniform2f(this.getLocation(), f1, f2);
	}

}