package be.zeldown.joid.impl.lwjgl2.render.shader.uniform;

import org.lwjgl.opengl.GL20;

import be.zeldown.joid.lib.bridge.render.shader.uniform.Float3Uniform;

public final class LWJGL2Float3Uniform extends LWJGL2ShaderUniform implements Float3Uniform {

	public LWJGL2Float3Uniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final float f1, final float f2, final float f3) {
		GL20.glUniform3f(super.getLocation(), f1, f2, f3);
	}

}