package dev.joid.base.opengl.render.texture;

import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.lib.bridge.render.texture.MipmapChain;

public interface IGlMipmapBuilder {

	public void build(final GlRenderBridge bridge, final GlTexture texture, final MipmapChain chain);

}