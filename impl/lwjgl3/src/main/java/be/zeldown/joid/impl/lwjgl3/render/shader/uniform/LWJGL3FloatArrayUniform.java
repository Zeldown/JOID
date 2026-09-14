package be.zeldown.joid.impl.lwjgl3.render.shader.uniform;

import org.lwjgl.opengl.GL20C;

import be.zeldown.joid.impl.lwjgl3.render.shader.LWJGL3Shader;
import be.zeldown.joid.lib.bridge.render.shader.uniform.FloatArrayUniform;
import lombok.NonNull;

public final class LWJGL3FloatArrayUniform extends LWJGL3ShaderUniform implements FloatArrayUniform {

	public LWJGL3FloatArrayUniform(final LWJGL3Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final @NonNull float[] value) {
		final int location = super.getLocation();
		final float[] copy = value.clone();
		super.getShader().queueUniform(location, () -> GL20C.glUniform1fv(location, copy));
	}

}