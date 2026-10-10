package dev.joid.base.opengl.render;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.binding.IGlBufferBinding;
import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import dev.joid.base.opengl.binding.IGlStateBinding;
import dev.joid.base.opengl.binding.IGlTextureBinding;
import dev.joid.base.opengl.capability.GlCapabilities;
import dev.joid.base.opengl.capability.GlStrategies;
import dev.joid.base.opengl.render.framebuffer.GlFrameBuffer;
import dev.joid.base.opengl.render.shader.GlShader;
import dev.joid.base.opengl.render.state.IGlStateGuard;
import dev.joid.base.opengl.render.state.JournalGlStateGuard;
import dev.joid.base.opengl.render.texture.GlTexture;
import dev.joid.base.opengl.render.texture.IGlMipmapBuilder;
import dev.joid.base.opengl.render.texture.IGlTexture;
import dev.joid.base.opengl.render.vertex.GlVertexInput;
import dev.joid.lib.bridge.render.RenderBridge;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.SamplerBinding;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.uniform.UniformSampler;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.state.StencilState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.bridge.render.vertex.VertexAttribute;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class GlRenderBridge extends RenderBridge {

	private final IGlBinding            binding;
	private final IGlStateGuard         guard;
	private final GlStrategies          strategies;
	private final GlVertexInput         vertexInput;
	private final GlCapabilities        capabilities;
	private final IGlMipmapBuilder      mipmapBuilder;
	private final IGlFrameBufferBinding frameBufferBinding;

	protected GlRenderBridge(final @NonNull IGlBinding binding) {
		this.capabilities       = GlCapabilities.read(binding);
		this.strategies         = GlStrategies.of(this.capabilities);
		this.guard              = JournalGlStateGuard.create(binding, this.capabilities, this.strategies.getFrameBufferFamily());
		this.binding            = this.guard.getBinding();
		this.mipmapBuilder      = this.strategies.createMipmapBuilder();
		this.frameBufferBinding = this.binding.getFrameBufferBinding(this.strategies.getFrameBufferFamily());
		this.guard.enter();
		try {
			this.vertexInput = this.strategies.createVertexInput(this.binding);
		} finally {
			this.guard.exit();
		}
	}

	public static @NonNull GlRenderBridge create(final @NonNull IGlBinding binding) {
		return new GlRenderBridge(binding);
	}

	@Override
	protected void beginFrameCommands() {
		this.guard.enter();
	}

	@Override
	protected void submitFrameCommands() {
		this.guard.exit();
	}

	@Override
	public void suspend(final @NonNull Runnable draw) {
		this.guard.suspend(draw);
	}

	@Override
	public void raster(final @NonNull IFrameBuffer target, final int width, final int height, final @NonNull Runnable draw) {
		this.guard.suspend(() -> {
			final int[] viewport = new int[4];
			final int previous = this.binding.getInteger(GlConstants.DRAW_FRAMEBUFFER_BINDING);
			this.binding.getIntegers(GlConstants.VIEWPORT, viewport);
			this.frameBufferBinding.bindFramebuffer(GlConstants.FRAMEBUFFER, ((GlFrameBuffer) target).getId());
			this.binding.getStateBinding().viewport(0, 0, width, height);
			try {
				draw.run();
			} finally {
				this.frameBufferBinding.bindFramebuffer(GlConstants.FRAMEBUFFER, previous);
				this.binding.getStateBinding().viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
			}
		});
	}

	@Override
	protected void clearColorBuffer(final float red, final float green, final float blue, final float alpha) {
		this.guard.enter();
		try {
			final RenderState state = super.getState();
			this.applyTarget(state);
			this.binding.getStateBinding().colorMask(true, true, true, true);
			this.binding.getStateBinding().clearColor(red, green, blue, alpha);
			this.binding.clear(GlConstants.COLOR_BUFFER_BIT);
		} finally {
			this.guard.exit();
		}
	}

	@Override
	protected void clearDepthBuffer() {
		this.guard.enter();
		try {
			this.applyTarget(super.getState());
			this.binding.getStateBinding().depthMask(true);
			this.binding.clear(GlConstants.DEPTH_BUFFER_BIT);
			this.binding.getStateBinding().depthMask(super.getState().isDepthWrite());
		} finally {
			this.guard.exit();
		}
	}

	@Override
	protected void clearStencilBuffer() {
		this.guard.enter();
		try {
			this.applyTarget(super.getState());
			this.binding.getStateBinding().stencilMask(0xFF);
			this.binding.clear(GlConstants.STENCIL_BUFFER_BIT);
		} finally {
			this.guard.exit();
		}
	}

	@Override
	protected void drawPrimitive(final @NonNull Primitive primitive, final @NonNull VertexBuffer buffer, final @NonNull IShader current) {
		this.guard.enter();
		try {
			final RenderState state = super.getState();
			this.applyTarget(state);
			this.applyPipeline(state);
			final GlShader shader = (GlShader) current;
			shader.use(state, buffer);
			this.bindTexture(0, super.resolveTexture());
			for (final UniformSampler sampler : shader.getSamplerMap().values()) {
				final int location = shader.getLocation(sampler.getName());
				if (location != -1) {
					this.bindTexture(sampler.getUnit(), super.resolveSampler(sampler));
					this.binding.getProgramBinding().uniform1i(location, sampler.getUnit());
				}
			}
			this.binding.getTextureBinding().activeTexture(GlConstants.TEXTURE0);

			final IGlBufferBinding vertices = this.binding.getBufferBinding();
			this.vertexInput.bind();
			vertices.bufferData(GlConstants.ARRAY_BUFFER, buffer.getBuffer(), GlConstants.STREAM_DRAW);
			this.toggleAttribute(VertexAttribute.TEXTURE_COORDINATE.getLocation(), buffer.isTexture());
			this.toggleAttribute(VertexAttribute.COLOR.getLocation(), buffer.isColor());
			this.toggleAttribute(VertexAttribute.NORMAL.getLocation(), buffer.isNormal());
			vertices.vertexAttrib2f(VertexAttribute.TEXTURE_COORDINATE.getLocation(), 0F, 0F);
			vertices.vertexAttrib3f(VertexAttribute.NORMAL.getLocation(), 0F, 0F, 1F);
			vertices.drawArrays(GlEnums.mode(primitive), 0, buffer.getCount());
		} finally {
			this.guard.exit();
		}
	}

	@Override
	public @NonNull ITexture createTexture() {
		return GlTexture.create(this);
	}

	@Override
	public @NonNull IFrameBuffer createFrameBuffer(final int width, final int height) {
		this.guard.enter();
		try {
			return GlFrameBuffer.create(this, width, height);
		} finally {
			this.guard.exit();
		}
	}

	@Override
	public @NonNull IShader createShader(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull BlendState blend) {
		this.guard.enter();
		try {
			return GlShader.create(this, vertex, fragment, blend);
		} finally {
			this.guard.exit();
		}
	}

	private void bindTexture(final int unit, final SamplerBinding resolved) {
		final IGlTextureBinding textures = this.binding.getTextureBinding();
		final IGlTexture texture = (IGlTexture) resolved.getTexture();
		textures.activeTexture(GlConstants.TEXTURE0 + unit);
		textures.bindTexture(GlConstants.TEXTURE_2D, texture.getId());
		texture.sample(resolved.getSampling());
		if (this.capabilities.hasSamplerObjects()) {
			textures.bindSampler(unit, 0);
		}
	}

	private void applyTarget(final RenderState state) {
		this.frameBufferBinding.bindFramebuffer(GlConstants.FRAMEBUFFER, state.getFrameBuffer() == null ? 0 : ((GlFrameBuffer) state.getFrameBuffer()).getId());
		this.binding.getStateBinding().viewport(state.getViewportX(), state.getViewportY(), state.getViewportWidth(), state.getViewportHeight());
	}

	private void applyPipeline(final RenderState state) {
		final IGlStateBinding pipeline = this.binding.getStateBinding();
		final BlendState blend = state.getBlend();
		this.toggle(GlConstants.BLEND, blend.isEnabled());
		if (blend.isEnabled()) {
			pipeline.blendEquationSeparate(GlEnums.equation(blend.getEquation()), GlEnums.equation(blend.getEquation()));
			pipeline.blendFuncSeparate(GlEnums.factor(blend.getSourceColor()), GlEnums.factor(blend.getDestinationColor()), GlEnums.factor(blend.getSourceAlpha()), GlEnums.factor(blend.getDestinationAlpha()));
		}

		this.toggle(GlConstants.DEPTH_TEST, state.isDepthTest());
		pipeline.depthMask(state.isDepthWrite());
		this.toggle(GlConstants.CULL_FACE, state.isCull());
		pipeline.colorMask(state.isColorWrite(), state.isColorWrite(), state.isColorWrite(), state.isColorWrite());

		final StencilState stencil = state.getStencil();
		this.toggle(GlConstants.STENCIL_TEST, stencil.isEnabled());
		pipeline.stencilFunc(GlEnums.function(stencil.getFunction()), stencil.getReference(), stencil.getMask());
		pipeline.stencilOp(GlEnums.operation(stencil.getFail()), GlEnums.operation(stencil.getDepthFail()), GlEnums.operation(stencil.getPass()));
	}

	private void toggle(final int capability, final boolean enabled) {
		if (enabled) {
			this.binding.enable(capability);
		} else {
			this.binding.disable(capability);
		}
	}

	private void toggleAttribute(final int location, final boolean enabled) {
		if (enabled) {
			this.binding.getBufferBinding().enableVertexAttribArray(location);
		} else {
			this.binding.getBufferBinding().disableVertexAttribArray(location);
		}
	}

}