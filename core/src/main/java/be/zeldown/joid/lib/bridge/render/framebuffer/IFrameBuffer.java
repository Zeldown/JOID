package be.zeldown.joid.lib.bridge.render.framebuffer;

import be.zeldown.joid.lib.bridge.render.texture.ITexture;
import lombok.NonNull;

public interface IFrameBuffer {

	public void delete();

	public int getWidth();
	public int getHeight();

	public @NonNull ITexture getTexture();

}