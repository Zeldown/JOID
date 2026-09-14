package be.zeldown.joid.impl.lwjgl3.render.shader.uniform;

import org.lwjgl.opengl.GL20C;

import be.zeldown.joid.impl.lwjgl3.render.shader.Shader;
import lombok.NonNull;

public final class FloatArrayUniform extends ShaderUniform implements be.zeldown.joid.lib.bridge.render.shader.uniform.FloatArrayUniform {

	public FloatArrayUniform(final Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final @NonNull float[] value) {
		final int location = super.getLocation();
		final float[] copy = value.clone();
		super.getShader().queueUniform(location, () -> GL20C.glUniform1fv(location, copy));
	}

}