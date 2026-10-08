package dev.joid.backend.lwjgl2.binding;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBufferBinding;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl2GlBufferBinding implements IGlBufferBinding {

	private int arrayBuffer;

	public static @NonNull Lwjgl2GlBufferBinding create() {
		return new Lwjgl2GlBufferBinding();
	}

	@Override
	public int genBuffer() {
		return GL15.glGenBuffers();
	}

	@Override
	public void bindBuffer(final int target, final int buffer) {
		if (target == GlConstants.ARRAY_BUFFER) {
			this.arrayBuffer = buffer;
		}
		GL15.glBindBuffer(target, buffer);
	}

	@Override
	public void bufferData(final int target, final @NonNull ByteBuffer data, final int usage) {
		GL15.glBufferData(target, data, usage);
	}

	@Override
	public int genVertexArray() {
		return GL30.glGenVertexArrays();
	}

	@Override
	public void bindVertexArray(final int array) {
		GL30.glBindVertexArray(array);
	}

	@Override
	public void enableVertexAttribArray(final int index) {
		GL20.glEnableVertexAttribArray(index);
	}

	@Override
	public void disableVertexAttribArray(final int index) {
		GL20.glDisableVertexAttribArray(index);
	}

	@Override
	public void vertexAttrib2f(final int index, final float x, final float y) {
		GL20.glVertexAttrib2f(index, x, y);
	}

	@Override
	public void vertexAttrib3f(final int index, final float x, final float y, final float z) {
		GL20.glVertexAttrib3f(index, x, y, z);
	}

	@Override
	public void vertexAttrib4f(final int index, final float x, final float y, final float z, final float w) {
		GL20.glVertexAttrib4f(index, x, y, z, w);
	}

	@Override
	public void vertexAttribPointer(final int index, final int size, final int type, final boolean normalized, final int stride, final long offset) {
		if (this.arrayBuffer != 0) {
			GL20.glVertexAttribPointer(index, size, type, normalized, stride, offset);
		}
	}

	@Override
	public void drawArrays(final int mode, final int first, final int count) {
		GL11.glDrawArrays(mode, first, count);
	}

}