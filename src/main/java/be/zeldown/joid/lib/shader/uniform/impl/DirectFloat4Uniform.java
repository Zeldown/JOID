package be.zeldown.joid.lib.shader.uniform.impl;

import org.lwjgl.opengl.GL20;

import be.zeldown.joid.lib.shader.uniform.Float4Uniform;

public class DirectFloat4Uniform extends DirectShaderUniform implements Float4Uniform {

	public DirectFloat4Uniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final float f1, final float f2, final float f3, final float f4) {
		GL20.glUniform4f(this.getLocation(), f1, f2, f3, f4);
	}

}