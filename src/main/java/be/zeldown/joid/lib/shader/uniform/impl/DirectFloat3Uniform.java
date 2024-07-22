package be.zeldown.joid.lib.shader.uniform.impl;

import org.lwjgl.opengl.GL20;

import be.zeldown.joid.lib.shader.uniform.Float3Uniform;

public class DirectFloat3Uniform extends DirectShaderUniform implements Float3Uniform {

	public DirectFloat3Uniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final float f1, final float f2, final float f3) {
		GL20.glUniform3f(this.getLocation(), f1, f2, f3);
	}

}