package be.zeldown.joid.impl.lwjgl2.render.shader.uniform;

import org.lwjgl.opengl.GL20;

import be.zeldown.joid.lib.bridge.render.shader.uniform.FloatUniform;

public final class LWJGL2FloatUniform extends LWJGL2ShaderUniform implements FloatUniform {

	public LWJGL2FloatUniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final float value) {
		GL20.glUniform1f(super.getLocation(), value);
	}

}