package be.zeldown.joid.lib.shader.uniform.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL20;

import be.zeldown.joid.lib.shader.uniform.Float4ArrayUniform;
import lombok.NonNull;

public class DirectFloat4ArrayUniform extends DirectShaderUniform implements Float4ArrayUniform {

	public DirectFloat4ArrayUniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final @NonNull float[] array, final int program) {
		if (array.length % 4 != 0) {
			throw new IllegalArgumentException("Invalid array size");
		}

		final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length * 4).order(ByteOrder.nativeOrder());
		final FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
		floatBuffer.put(array);
		floatBuffer.rewind();

		final ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(array.length * 4).order(ByteOrder.nativeOrder());
		final FloatBuffer floatBuffer2 = byteBuffer2.asFloatBuffer();
		byteBuffer2.rewind();

		GL20.glUniform4(this.getLocation(), floatBuffer);
		GL20.glGetUniform(program, this.getLocation(), floatBuffer2);
	}

}