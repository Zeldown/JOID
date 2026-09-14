package be.zeldown.joid.impl.lwjgl2.render.shader.uniform;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import be.zeldown.joid.lib.bridge.render.shader.uniform.Float4ArrayUniform;
import lombok.NonNull;

public final class LWJGL2Float4ArrayUniform extends LWJGL2ShaderUniform implements Float4ArrayUniform {

	public LWJGL2Float4ArrayUniform(final int location) {
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