package be.zeldown.joid.impl.lwjgl2.render.shader.uniform;

import org.lwjgl.opengl.GL20;


public final class BooleanUniform extends ShaderUniform implements be.zeldown.joid.lib.bridge.render.shader.uniform.BooleanUniform {

	public BooleanUniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final boolean value) {
		GL20.glUniform1i(super.getLocation(), value ? 1 : 0);
	}

}