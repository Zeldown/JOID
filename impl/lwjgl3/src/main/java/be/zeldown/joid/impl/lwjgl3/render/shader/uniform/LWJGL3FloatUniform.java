package be.zeldown.joid.impl.lwjgl3.render.shader.uniform;

import org.lwjgl.opengl.GL20C;

import be.zeldown.joid.impl.lwjgl3.render.shader.LWJGL3Shader;
import be.zeldown.joid.lib.bridge.render.shader.uniform.FloatUniform;

public final class LWJGL3FloatUniform extends LWJGL3ShaderUniform implements FloatUniform {

	public LWJGL3FloatUniform(final LWJGL3Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final float value) {
		final int location = super.getLocation();
		super.getShader().queueUniform(location, () -> GL20C.glUniform1f(location, value));
	}

}