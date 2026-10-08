package dev.joid.base.opengl.binding;

import lombok.NonNull;

public interface IGlBinding {

	public void enable(final int capability);
	public void disable(final int capability);
	public boolean isEnabled(final int capability);

	public int getInteger(final int name);
	public String getString(final int name);
	public String getString(final int name, final int index);
	public void getFloats(final int name, final @NonNull float[] values);

	public @NonNull IGlStateBinding getStateBinding();
	public @NonNull IGlBufferBinding getBufferBinding();
	public @NonNull IGlProgramBinding getProgramBinding();
	public @NonNull IGlTextureBinding getTextureBinding();
	public @NonNull IGlFrameBufferBinding getFrameBufferBinding();

}