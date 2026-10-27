package com.example.backend;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.example.backend.engine.EngineDevice;

import dev.joid.lib.bridge.render.RenderBridge;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.SamplerBinding;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.uniform.UniformSampler;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import dev.joid.lib.bridge.render.vertex.VertexFill;

// #region bridge
public final class EngineRenderBridge extends RenderBridge {

	private final EngineDevice device;

	private ByteBuffer vertices;

	public EngineRenderBridge(final EngineDevice device) {
		this.device   = device;
		this.vertices = ByteBuffer.allocateDirect(VertexBuffer.STRIDE * 4096).order(ByteOrder.nativeOrder());
	}

	public EngineDevice getDevice() {
		return this.device;
	}

	// #region draw
	@Override
	protected void drawPrimitive(final Primitive primitive, final VertexBuffer buffer, final IShader shader) {
		super.requireFrame();
		final RenderState state = super.getState();
		this.applyTarget(state);
		this.device.blend(state.getBlend());
		this.device.depth(state.isDepthTest(), state.isDepthWrite());
		this.device.cull(state.isCull());
		this.device.colorMask(state.isColorWrite());
		this.device.stencil(state.getStencil());

		final EngineShader program = (EngineShader) shader;
		for (final UniformSampler sampler : program.getSamplerMap().values()) {
			final SamplerBinding binding = super.resolveSampler(sampler);
			final IEngineTexture texture = (IEngineTexture) binding.getTexture();
			this.device.texture(sampler.getUnit(), texture.getId(), binding.getSampling());
		}

		program.upload(state, buffer);
		final ByteBuffer vertices = VertexFill.complete(buffer, this.getVertices(buffer));
		this.device.draw(primitive == Primitive.LINES, vertices, buffer.getCount(), program.getProgram());
	}
	// #endregion

	@Override
	protected void clearColorBuffer(final float red, final float green, final float blue, final float alpha) {
		super.requireFrame();
		this.applyTarget(super.getState());
		this.device.clearColor(red, green, blue, alpha);
	}

	@Override
	protected void clearDepthBuffer() {
		super.requireFrame();
		this.applyTarget(super.getState());
		this.device.clearDepth(1F);
	}

	@Override
	protected void clearStencilBuffer() {
		super.requireFrame();
		this.applyTarget(super.getState());
		this.device.clearStencil(0);
	}

	@Override
	public ITexture createTexture() {
		return new EngineTexture(this.device);
	}

	@Override
	public IFrameBuffer createFrameBuffer(final int width, final int height) {
		return new EngineFrameBuffer(this.device, width, height);
	}

	@Override
	public IShader createShader(final ShaderSource vertex, final ShaderSource fragment, final BlendState blend) {
		return new EngineShader(this, vertex, fragment, blend);
	}

	@Override
	public void suspend(final Runnable draw) {
		draw.run();
	}

	@Override
	public void raster(final IFrameBuffer target, final int width, final int height, final Runnable draw) {
		super.requireFrame();
		this.device.target(((EngineFrameBuffer) target).getTarget());
		this.device.viewport(0, 0, width, height);
		draw.run();
	}

	private void applyTarget(final RenderState state) {
		final IFrameBuffer target = state.getFrameBuffer();
		this.device.target(target == null ? 0 : ((EngineFrameBuffer) target).getTarget());
		final int width = state.getViewportWidth();
		final int height = state.getViewportHeight();
		this.device.viewport(state.getViewportX(), state.getViewportY(), width, height);
	}

	private ByteBuffer getVertices(final VertexBuffer buffer) {
		final int size = buffer.getCount() * VertexBuffer.STRIDE;
		if (this.vertices.capacity() < size) {
			this.vertices = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
		}

		this.vertices.clear();
		return this.vertices;
	}

}
// #endregion