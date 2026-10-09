package dev.joid.base.opengl.render.texture;

import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureSampling;
import lombok.NonNull;

public interface IGlTexture extends ITexture {

	public int getId();

	public void sample(final @NonNull TextureSampling sampling);

}