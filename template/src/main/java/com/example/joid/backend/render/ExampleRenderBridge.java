package com.example.joid.backend.render;

import dev.joid.lib.bridge.render.RenderBridge;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;

public final class ExampleRenderBridge extends RenderBridge {

	@Override
	protected void clearDepthBuffer() {
		throw new UnsupportedOperationException("ExampleRenderBridge.clearDepthBuffer() is not implemented, implement it with the API of your engine");
	}

	@Override
	protected void clearStencilBuffer() {
		throw new UnsupportedOperationException("ExampleRenderBridge.clearStencilBuffer() is not implemented, implement it with the API of your engine");
	}

	@Override
	public ITexture createTexture() {
		throw new UnsupportedOperationException("ExampleRenderBridge.createTexture() is not implemented, implement it with the API of your engine");
	}

	@Override
	protected void clearColorBuffer(final float red, final float green, final float blue, final float alpha) {
		throw new UnsupportedOperationException("ExampleRenderBridge.clearColorBuffer() is not implemented, implement it with the API of your engine");
	}

	@Override
	protected void drawPrimitive(final Primitive primitive, final VertexBuffer buffer, final IShader shader) {
		throw new UnsupportedOperationException("ExampleRenderBridge.drawPrimitive() is not implemented, implement it with the API of your engine");
	}

	@Override
	public IFrameBuffer createFrameBuffer(final int width, final int height) {
		throw new UnsupportedOperationException("ExampleRenderBridge.createFrameBuffer() is not implemented, implement it with the API of your engine");
	}

	@Override
	public IShader createShader(final ShaderSource vertex, final ShaderSource fragment, final BlendState blend) {
		throw new UnsupportedOperationException("ExampleRenderBridge.createShader() is not implemented, implement it with the API of your engine");
	}

}