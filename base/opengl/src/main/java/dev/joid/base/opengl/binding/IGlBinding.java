package dev.joid.base.opengl.binding;

import java.nio.ByteBuffer;

import dev.joid.base.opengl.capability.GlFrameBufferFamily;
import lombok.NonNull;

public interface IGlBinding {

	public void enable(final int capability);
	public void disable(final int capability);
	public boolean isEnabled(final int capability);

	public int getInteger(final int name);
	public String getString(final int name);
	public String getString(final int name, final int index);
	public void getFloats(final int name, final @NonNull float[] values);
	public void getIntegers(final int name, final @NonNull int[] values);

	public int getVertexAttribi(final int index, final int name);
	public int getTexParameteri(final int target, final int name);
	public long getVertexAttribPointer(final int index, final int name);

	public void clear(final int mask);
	public void readBuffer(final int buffer);
	public void readPixels(final int x, final int y, final int width, final int height, final int format, final int type, final @NonNull ByteBuffer pixels);

	public @NonNull IGlStateBinding getStateBinding();
	public @NonNull IGlBufferBinding getBufferBinding();
	public @NonNull IGlProgramBinding getProgramBinding();
	public @NonNull IGlTextureBinding getTextureBinding();
	public @NonNull IGlFrameBufferBinding getFrameBufferBinding(final @NonNull GlFrameBufferFamily family);

}