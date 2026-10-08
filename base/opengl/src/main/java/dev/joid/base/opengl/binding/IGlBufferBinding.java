package dev.joid.base.opengl.binding;

import java.nio.ByteBuffer;

import lombok.NonNull;

public interface IGlBufferBinding {

	public int genBuffer();
	public void bindBuffer(final int target, final int buffer);
	public void bufferData(final int target, final @NonNull ByteBuffer data, final int usage);

	public int genVertexArray();
	public void bindVertexArray(final int array);

	public void enableVertexAttribArray(final int index);
	public void disableVertexAttribArray(final int index);
	public void vertexAttrib2f(final int index, final float x, final float y);
	public void vertexAttrib3f(final int index, final float x, final float y, final float z);
	public void vertexAttrib4f(final int index, final float x, final float y, final float z, final float w);
	public void vertexAttribPointer(final int index, final int size, final int type, final boolean normalized, final int stride, final long offset);

	public void drawArrays(final int mode, final int first, final int count);

}