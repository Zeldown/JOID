package be.zeldown.joid.lib.shader.uniform.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL20;

import be.zeldown.joid.lib.shader.uniform.FloatArrayUniform;
import lombok.NonNull;

public class DirectFloatArrayUniform extends DirectShaderUniform implements FloatArrayUniform {

	public DirectFloatArrayUniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final @NonNull float[] array) {
		final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length * 4).order(ByteOrder.nativeOrder());
		final FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
		floatBuffer.put(array);
		floatBuffer.rewind();
		GL20.glUniform1(this.getLocation(), floatBuffer);
	}

}