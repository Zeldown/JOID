package dev.joid.base.opengl.render.texture;

import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.lib.bridge.render.texture.MipmapChain;
import lombok.NonNull;

public interface IGlMipmapBuilder {

	public void build(final @NonNull GlRenderBridge bridge, final @NonNull GlTexture texture, final @NonNull MipmapChain chain);

}