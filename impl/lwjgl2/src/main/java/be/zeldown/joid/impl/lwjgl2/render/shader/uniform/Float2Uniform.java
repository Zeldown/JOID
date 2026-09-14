package be.zeldown.joid.impl.lwjgl2.render.shader.uniform;

import org.lwjgl.opengl.GL20;


public final class Float2Uniform extends ShaderUniform implements be.zeldown.joid.lib.bridge.render.shader.uniform.Float2Uniform {

	public Float2Uniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final float f1, final float f2) {
		GL20.glUniform2f(super.getLocation(), f1, f2);
	}

}