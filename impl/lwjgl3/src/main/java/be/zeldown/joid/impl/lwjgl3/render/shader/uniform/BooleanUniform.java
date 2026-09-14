package be.zeldown.joid.impl.lwjgl3.render.shader.uniform;

import org.lwjgl.opengl.GL20C;

import be.zeldown.joid.impl.lwjgl3.render.shader.Shader;

public final class BooleanUniform extends ShaderUniform implements be.zeldown.joid.lib.bridge.render.shader.uniform.BooleanUniform {

	public BooleanUniform(final Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final boolean value) {
		final int location = super.getLocation();
		super.getShader().queueUniform(location, () -> GL20C.glUniform1i(location, value ? 1 : 0));
	}

}