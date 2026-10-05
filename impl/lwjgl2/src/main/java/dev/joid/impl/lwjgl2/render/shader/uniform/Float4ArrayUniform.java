package dev.joid.impl.lwjgl2.render.shader.uniform;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import lombok.NonNull;

public final class Float4ArrayUniform extends ShaderUniform implements dev.joid.lib.bridge.render.shader.uniform.Float4ArrayUniform {

	public Float4ArrayUniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final @NonNull float[] array) {
		if (array.length % 4 != 0) {
			throw new IllegalArgumentException("Invalid array size");
		}

		final FloatBuffer buffer = BufferUtils.createFloatBuffer(array.length);
		buffer.put(array).flip();
		GL20.glUniform4(super.getLocation(), buffer);
	}

}