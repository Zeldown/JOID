package be.zeldown.joid.impl.lwjgl2.render.shader.uniform;

import org.lwjgl.opengl.GL20;


public final class Float4Uniform extends ShaderUniform implements be.zeldown.joid.lib.bridge.render.shader.uniform.Float4Uniform {

	public Float4Uniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final float f1, final float f2, final float f3, final float f4) {
		GL20.glUniform4f(super.getLocation(), f1, f2, f3, f4);
	}

}