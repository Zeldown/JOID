package dev.joid.lib.bridge.render.framebuffer;

import dev.joid.lib.bridge.render.texture.ITexture;
import lombok.NonNull;

public interface IFrameBuffer {

	public int getWidth();
	public int getHeight();
	public @NonNull ITexture getTexture();

	public void delete();

}