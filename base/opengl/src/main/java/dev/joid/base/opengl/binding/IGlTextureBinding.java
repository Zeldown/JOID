package dev.joid.base.opengl.binding;

import lombok.NonNull;

public interface IGlTextureBinding {

	public int genTexture();
	public void activeTexture(final int unit);
	public void deleteTexture(final int texture);
	public void bindSampler(final int unit, final int sampler);
	public void bindTexture(final int target, final int texture);
	public void texParameteri(final int target, final int name, final int value);
	public void texImage2D(final int target, final int level, final int internalFormat, final int width, final int height, final int format, final int type);
	public void texSubImage2D(final int target, final int level, final int x, final int y, final int width, final int height, final int format, final int type, final @NonNull int[] pixels);

}