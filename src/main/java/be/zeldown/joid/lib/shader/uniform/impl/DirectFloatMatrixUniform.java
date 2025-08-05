package be.zeldown.joid.lib.shader.uniform.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL20;

import be.zeldown.joid.lib.shader.uniform.FloatMatrixUniform;
import lombok.NonNull;

public class DirectFloatMatrixUniform extends DirectShaderUniform implements FloatMatrixUniform {

	public DirectFloatMatrixUniform(final int location) {
		super(location);
	}

	@Override
	public void setValue(final @NonNull float[] array) {
		final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length * 4).order(ByteOrder.nativeOrder());
		final FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
		floatBuffer.put(array);
		floatBuffer.rewind();

		switch (array.length) {
			case 4:
				GL20.glUniformMatrix2(this.getLocation(), false, floatBuffer);
				break;
			case 9:
				GL20.glUniformMatrix3(this.getLocation(), false, floatBuffer);
				break;
			case 16:
				GL20.glUniformMatrix4(this.getLocation(), false, floatBuffer);
				break;
			default:
				throw new IllegalArgumentException("Invalid matrix size");
		}
	}

}