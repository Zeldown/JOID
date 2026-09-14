package be.zeldown.joid.impl.lwjgl2.render.shader.uniform;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import lombok.NonNull;

public final class FloatArrayUniform extends ShaderUniform implements be.zeldown.joid.lib.bridge.render.shader.uniform.FloatArrayUniform {

	public FloatArrayUniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final @NonNull float[] value) {
		final FloatBuffer buffer = BufferUtils.createFloatBuffer(value.length);
		buffer.put(value).flip();
		GL20.glUniform1(super.getLocation(), buffer);
	}

}