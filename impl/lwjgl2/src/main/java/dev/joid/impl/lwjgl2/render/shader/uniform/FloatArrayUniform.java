package dev.joid.impl.lwjgl2.render.shader.uniform;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import dev.joid.impl.lwjgl2.render.shader.Shader;
import lombok.NonNull;

public final class FloatArrayUniform extends ShaderUniform implements dev.joid.lib.bridge.render.shader.uniform.FloatArrayUniform {

	public FloatArrayUniform(final Shader shader, final int location) {
		super(shader, location);
	}

	@Override
	public void setValue(final @NonNull float[] value) {
		final int location = super.getLocation();
		final FloatBuffer buffer = BufferUtils.createFloatBuffer(value.length);
		buffer.put(value).flip();
		super.getShader().queueUniform(location, () -> GL20.glUniform1(location, buffer));
	}

}