package be.zeldown.joid.impl.lwjgl3.render.shader.uniform;

import org.lwjgl.opengl.GL20C;

import be.zeldown.joid.impl.lwjgl3.render.shader.LWJGL3Shader;
import be.zeldown.joid.lib.bridge.render.shader.uniform.BooleanUniform;

public final class LWJGL3BooleanUniform extends LWJGL3ShaderUniform implements BooleanUniform {

	public LWJGL3BooleanUniform(final LWJGL3Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final boolean value) {
		final int location = super.getLocation();
		super.getShader().queueUniform(location, () -> GL20C.glUniform1i(location, value ? 1 : 0));
	}

}