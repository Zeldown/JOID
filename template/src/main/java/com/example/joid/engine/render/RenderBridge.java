package com.example.joid.engine.render;

import be.zeldown.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import be.zeldown.joid.lib.bridge.render.shader.IShader;
import be.zeldown.joid.lib.bridge.render.shader.source.ShaderSource;
import be.zeldown.joid.lib.bridge.render.state.BlendState;
import be.zeldown.joid.lib.bridge.render.texture.ITexture;
import be.zeldown.joid.lib.bridge.render.texture.TextureFilter;
import be.zeldown.joid.lib.bridge.render.vertex.DrawMode;
import be.zeldown.joid.lib.bridge.render.vertex.VertexBuffer;

public final class RenderBridge extends be.zeldown.joid.lib.bridge.render.RenderBridge {

	public void endFrame() {
		throw new UnsupportedOperationException();
	}

	public void beginFrame() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearStencil() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ITexture createTexture() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void draw(final DrawMode mode, final VertexBuffer buffer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear(final float red, final float green, final float blue, final float alpha) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IFrameBuffer createFrameBuffer(final int width, final int height, final TextureFilter filter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IShader createShader(final ShaderSource vertex, final ShaderSource fragment, final BlendState blend) {
		throw new UnsupportedOperationException();
	}

}