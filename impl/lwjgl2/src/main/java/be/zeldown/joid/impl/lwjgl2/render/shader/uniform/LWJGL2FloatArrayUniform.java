package be.zeldown.joid.impl.lwjgl2.render.shader.uniform;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import be.zeldown.joid.lib.bridge.render.shader.uniform.FloatArrayUniform;
import lombok.NonNull;

public final class LWJGL2FloatArrayUniform extends LWJGL2ShaderUniform implements FloatArrayUniform {

	public LWJGL2FloatArrayUniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final @NonNull float[] value) {
		final FloatBuffer buffer = BufferUtils.createFloatBuffer(value.length);
		buffer.put(value).flip();
		GL20.glUniform1(super.getLocation(), buffer);
	}

}