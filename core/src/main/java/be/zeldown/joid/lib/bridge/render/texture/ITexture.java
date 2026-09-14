package be.zeldown.joid.lib.bridge.render.texture;

import lombok.NonNull;

public interface ITexture {

	public @NonNull ITexture allocate(final int width, final int height);
	public @NonNull ITexture upload(final @NonNull int[] pixels, final int width, final int height);

	public int getWidth();
	public int getHeight();

	public void delete();

}