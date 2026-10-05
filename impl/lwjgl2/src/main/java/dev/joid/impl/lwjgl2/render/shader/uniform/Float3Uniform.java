package dev.joid.impl.lwjgl2.render.shader.uniform;

import org.lwjgl.opengl.GL20;


public final class Float3Uniform extends ShaderUniform implements dev.joid.lib.bridge.render.shader.uniform.Float3Uniform {

	public Float3Uniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final float f1, final float f2, final float f3) {
		GL20.glUniform3f(super.getLocation(), f1, f2, f3);
	}

}