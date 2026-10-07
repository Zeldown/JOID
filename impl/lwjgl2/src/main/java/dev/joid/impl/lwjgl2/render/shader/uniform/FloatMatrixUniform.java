package dev.joid.impl.lwjgl2.render.shader.uniform;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import dev.joid.impl.lwjgl2.render.shader.Shader;
import lombok.NonNull;

public final class FloatMatrixUniform extends ShaderUniform implements dev.joid.lib.bridge.render.shader.uniform.FloatMatrixUniform {

	public FloatMatrixUniform(final Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final @NonNull float[] value) {
		final int location = super.getLocation();
		final FloatBuffer buffer = BufferUtils.createFloatBuffer(value.length);
		buffer.put(value).flip();

		switch (value.length) {
		case 4:
			super.getShader().queueUniform(location, () -> GL20.glUniformMatrix2(location, false, buffer));
			break;
		case 9:
			super.getShader().queueUniform(location, () -> GL20.glUniformMatrix3(location, false, buffer));
			break;
		case 16:
			super.getShader().queueUniform(location, () -> GL20.glUniformMatrix4(location, false, buffer));
			break;
		default:
			throw new IllegalArgumentException("Invalid matrix size");
		}
	}

}