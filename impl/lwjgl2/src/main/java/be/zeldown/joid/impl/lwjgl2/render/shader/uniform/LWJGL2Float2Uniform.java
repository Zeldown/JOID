package be.zeldown.joid.impl.lwjgl2.render.shader.uniform;

import org.lwjgl.opengl.GL20;

import be.zeldown.joid.lib.bridge.render.shader.uniform.Float2Uniform;

public final class LWJGL2Float2Uniform extends LWJGL2ShaderUniform implements Float2Uniform {

	public LWJGL2Float2Uniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final float f1, final float f2) {
		GL20.glUniform2f(super.getLocation(), f1, f2);
	}

}