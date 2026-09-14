package be.zeldown.joid.impl.lwjgl2.render.shader.uniform;

import org.lwjgl.opengl.GL20;


public final class FloatUniform extends ShaderUniform implements be.zeldown.joid.lib.bridge.render.shader.uniform.FloatUniform {

	public FloatUniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final float value) {
		GL20.glUniform1f(super.getLocation(), value);
	}

}