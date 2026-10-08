package dev.joid.impl.lwjgl3.binding;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import dev.joid.impl.opengl.binding.IGlBufferBinding;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl3GlBufferBinding implements IGlBufferBinding {

	public static @NonNull Lwjgl3GlBufferBinding create() {
		return new Lwjgl3GlBufferBinding();
	}

	@Override
	public int genBuffer() {
		return GL15C.glGenBuffers();
	}

	@Override
	public void bindBuffer(final int target, final int buffer) {
		GL15C.glBindBuffer(target, buffer);
	}

	@Override
	public void bindBufferBase(final int target, final int index, final int buffer) {
		GL30C.glBindBufferBase(target, index, buffer);
	}

	@Override
	public void bufferData(final int target, final @NonNull ByteBuffer data, final int usage) {
		GL15C.glBufferData(target, data, usage);
	}

	@Override
	public void bufferSubData(final int target, final long offset, final @NonNull ByteBuffer data) {
		GL15C.glBufferSubData(target, offset, data);
	}

	@Override
	public int genVertexArray() {
		return GL30C.glGenVertexArrays();
	}

	@Override
	public void bindVertexArray(final int array) {
		GL30C.glBindVertexArray(array);
	}

	@Override
	public void enableVertexAttribArray(final int index) {
		GL20C.glEnableVertexAttribArray(index);
	}

	@Override
	public void disableVertexAttribArray(final int index) {
		GL20C.glDisableVertexAttribArray(index);
	}

	@Override
	public void vertexAttrib2f(final int index, final float x, final float y) {
		GL20C.glVertexAttrib2f(index, x, y);
	}

	@Override
	public void vertexAttrib3f(final int index, final float x, final float y, final float z) {
		GL20C.glVertexAttrib3f(index, x, y, z);
	}

	@Override
	public void vertexAttrib4f(final int index, final float x, final float y, final float z, final float w) {
		GL20C.glVertexAttrib4f(index, x, y, z, w);
	}

	@Override
	public void vertexAttribPointer(final int index, final int size, final int type, final boolean normalized, final int stride, final long offset) {
		GL20C.glVertexAttribPointer(index, size, type, normalized, stride, offset);
	}

	@Override
	public void drawArrays(final int mode, final int first, final int count) {
		GL11C.glDrawArrays(mode, first, count);
	}

}