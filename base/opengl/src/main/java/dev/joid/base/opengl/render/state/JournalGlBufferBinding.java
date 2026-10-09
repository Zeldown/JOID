package dev.joid.base.opengl.render.state;

import java.nio.ByteBuffer;

import dev.joid.base.opengl.binding.IGlBufferBinding;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class JournalGlBufferBinding implements IGlBufferBinding {

	private final IGlBufferBinding binding;
	private final GlStateJournal   journal;

	public static @NonNull JournalGlBufferBinding create(final @NonNull IGlBufferBinding binding, final @NonNull GlStateJournal journal) {
		return new JournalGlBufferBinding(binding, journal);
	}

	@Override
	public int genBuffer() {
		return this.binding.genBuffer();
	}

	@Override
	public void bindBuffer(final int target, final int buffer) {
		this.journal.touch(GlStateKey.BUFFER, target);
		this.binding.bindBuffer(target, buffer);
	}

	@Override
	public void bufferData(final int target, final @NonNull ByteBuffer data, final int usage) {
		this.binding.bufferData(target, data, usage);
	}

	@Override
	public int genVertexArray() {
		final int array = this.binding.genVertexArray();
		this.journal.excludeVertexArray(array);
		return array;
	}

	@Override
	public void bindVertexArray(final int array) {
		this.journal.touch(GlStateKey.VERTEX_ARRAY);
		this.journal.trackVertexArray(array);
		this.binding.bindVertexArray(array);
	}

	@Override
	public void enableVertexAttribArray(final int index) {
		this.journal.saveVertexAttribute(index);
		this.binding.enableVertexAttribArray(index);
	}

	@Override
	public void disableVertexAttribArray(final int index) {
		this.journal.saveVertexAttribute(index);
		this.binding.disableVertexAttribArray(index);
	}

	@Override
	public void vertexAttrib2f(final int index, final float x, final float y) {
		this.journal.touch(GlStateKey.CURRENT_VERTEX_ATTRIBUTE, index);
		this.binding.vertexAttrib2f(index, x, y);
	}

	@Override
	public void vertexAttrib3f(final int index, final float x, final float y, final float z) {
		this.journal.touch(GlStateKey.CURRENT_VERTEX_ATTRIBUTE, index);
		this.binding.vertexAttrib3f(index, x, y, z);
	}

	@Override
	public void vertexAttrib4f(final int index, final float x, final float y, final float z, final float w) {
		this.journal.touch(GlStateKey.CURRENT_VERTEX_ATTRIBUTE, index);
		this.binding.vertexAttrib4f(index, x, y, z, w);
	}

	@Override
	public void vertexAttribPointer(final int index, final int size, final int type, final boolean normalized, final int stride, final long offset) {
		this.journal.saveVertexAttribute(index);
		this.binding.vertexAttribPointer(index, size, type, normalized, stride, offset);
	}

	@Override
	public void drawArrays(final int mode, final int first, final int count) {
		this.binding.drawArrays(mode, first, count);
	}

}