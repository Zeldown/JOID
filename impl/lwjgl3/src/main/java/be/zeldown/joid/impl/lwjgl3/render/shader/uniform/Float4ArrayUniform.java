package be.zeldown.joid.impl.lwjgl3.render.shader.uniform;

import org.lwjgl.opengl.GL20C;

import be.zeldown.joid.impl.lwjgl3.render.shader.Shader;
import lombok.NonNull;

public final class Float4ArrayUniform extends ShaderUniform implements be.zeldown.joid.lib.bridge.render.shader.uniform.Float4ArrayUniform {

	public Float4ArrayUniform(final Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final @NonNull float[] array) {
		if (array.length % 4 != 0) {
			throw new IllegalArgumentException("Invalid array size");
		}

		final int location = super.getLocation();
		final float[] copy = array.clone();
		super.getShader().queueUniform(location, () -> GL20C.glUniform4fv(location, copy));
	}

}