package dev.joid.impl.lwjgl2.render.shader.uniform;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import lombok.NonNull;

public final class FloatMatrixUniform extends ShaderUniform implements dev.joid.lib.bridge.render.shader.uniform.FloatMatrixUniform {

	public FloatMatrixUniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final @NonNull float[] value) {
		final FloatBuffer buffer = BufferUtils.createFloatBuffer(value.length);
		buffer.put(value).flip();

		switch (value.length) {
		case 4:
			GL20.glUniformMatrix2(super.getLocation(), false, buffer);
			break;
		case 9:
			GL20.glUniformMatrix3(super.getLocation(), false, buffer);
			break;
		case 16:
			GL20.glUniformMatrix4(super.getLocation(), false, buffer);
			break;
		default:
			throw new IllegalArgumentException("Invalid matrix size");
		}
	}

}