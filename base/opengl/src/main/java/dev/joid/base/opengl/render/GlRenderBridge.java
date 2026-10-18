package dev.joid.base.opengl.render;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import dev.joid.base.opengl.capability.GlCapabilities;
import dev.joid.base.opengl.render.framebuffer.GlFrameBuffer;
import dev.joid.base.opengl.render.shader.GlShader;
import dev.joid.base.opengl.render.state.GlStateGuard;
import dev.joid.base.opengl.render.texture.BlitGlMipmapBuilder;
import dev.joid.base.opengl.render.texture.DrawGlMipmapBuilder;
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
	private final GlStateGuard          guard;
	private final GlVertexInput         vertexInput;
	private final GlCapabilities        capabilities;
	private final IGlMipmapBuilder      mipmapBuilder;
	private final IGlFrameBufferBinding frameBufferBinding;

	private int screenFrameBuffer;

	public GlRenderBridge(final @NonNull IGlBinding binding) {
		this.capabilities       = GlCapabilities.read(binding).require();
		this.guard              = new GlStateGuard(binding, this.capabilities);
		this.binding            = this.guard.getBinding();
		this.mipmapBuilder      = this.capabilities.hasFrameBufferBlit() ? new BlitGlMipmapBuilder() : new DrawGlMipmapBuilder();
		this.frameBufferBinding = this.binding.getFrameBufferBinding(this.capabilities.getFrameBufferFamily());
		this.vertexInput        = this.guard.call(() -> new GlVertexInput(this.binding, this.capabilities.hasVertexArrays()));
	}

	@Override
	protected void beginFrameCommands() {
		this.screenFrameBuffer = this.binding.getInteger(GlConstants.DRAW_FRAMEBUFFER_BINDING);
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
			final int previousRead = this.capabilities.hasFrameBufferBlit() ? this.binding.getInteger(GlConstants.READ_FRAMEBUFFER_BINDING) : previous;
			this.binding.getIntegers(GlConstants.VIEWPORT, viewport);
			this.frameBufferBinding.bindFramebuffer(GlConstants.FRAMEBUFFER, ((GlFrameBuffer) target).getId());
			this.binding.viewport(0, 0, width, height);
			try {
				draw.run();
			} finally {
				this.frameBufferBinding.bindFramebuffer(GlConstants.FRAMEBUFFER, previous);
				if (previousRead != previous) {
					this.frameBufferBinding.bindFramebuffer(GlConstants.READ_FRAMEBUFFER, previousRead);
				}
				this.binding.viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
			}
		});
	}

	@Override
	protected void clearColorBuffer(final float red, final float green, final float blue, final float alpha) {
		this.clearBuffer(GlConstants.COLOR_BUFFER_BIT, () -> {
			this.binding.colorMask(true, true, true, true);
			this.binding.clearColor(red, green, blue, alpha);
		});
	}

	@Override
	protected void clearDepthBuffer() {
		this.clearBuffer(GlConstants.DEPTH_BUFFER_BIT, () -> this.binding.depthMask(true));
	}

	@Override
	protected void clearStencilBuffer() {
		this.clearBuffer(GlConstants.STENCIL_BUFFER_BIT, () -> this.binding.stencilMask(0xFF));
	}

	@Override
	protected void drawPrimitive(final @NonNull Primitive primitive, final @NonNull VertexBuffer buffer, final @NonNull IShader current) {
		this.guard.run(() -> {
			final RenderState state = super.getState();
			this.applyTarget(state);
			this.applyPipeline(state);
			final GlShader shader = (GlShader) current;
			shader.use(state, buffer);
			for (final UniformSampler sampler : shader.getSamplerMap().values()) {
				final int location = shader.getLocation(sampler.getName());
				if (location != -1) {
					this.bindTexture(sampler.getUnit(), super.resolveSampler(sampler));
					this.binding.uniform1i(location, sampler.getUnit());
				}
			}
			this.binding.activeTexture(GlConstants.TEXTURE0);

			this.vertexInput.bind();
			this.binding.bufferData(GlConstants.ARRAY_BUFFER, buffer.getBuffer(), GlConstants.STREAM_DRAW);
			this.toggleAttribute(VertexAttribute.TEXTURE_COORDINATE.getLocation(), buffer.isTexture());
			this.toggleAttribute(VertexAttribute.COLOR.getLocation(), buffer.isColor());
			this.toggleAttribute(VertexAttribute.NORMAL.getLocation(), buffer.isNormal());
			this.binding.vertexAttrib2f(VertexAttribute.TEXTURE_COORDINATE.getLocation(), 0F, 0F);
			this.binding.vertexAttrib3f(VertexAttribute.NORMAL.getLocation(), 0F, 0F, 1F);
			this.binding.drawArrays(GlEnums.mode(primitive), 0, buffer.getCount());
		});
	}

	@Override
	public @NonNull ITexture createTexture() {
		return GlTexture.create(this);
	}

	@Override
	public @NonNull IFrameBuffer createFrameBuffer(final int width, final int height) {
		return this.guard.call(() -> GlFrameBuffer.create(this, width, height));
	}

	@Override
	public @NonNull IShader createShader(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull BlendState blend) {
		return this.guard.call(() -> GlShader.create(this, vertex, fragment, blend));
	}

	private void bindTexture(final int unit, final SamplerBinding resolved) {
		final IGlTexture texture = (IGlTexture) resolved.getTexture();
		this.binding.activeTexture(GlConstants.TEXTURE0 + unit);
		this.binding.bindTexture(GlConstants.TEXTURE_2D, texture.getId());
		texture.sample(resolved.getSampling());
		if (this.capabilities.hasSamplerObjects()) {
			this.binding.bindSampler(unit, 0);
		}
	}

	private void clearBuffer(final int mask, final Runnable prepare) {
		this.guard.run(() -> {
			this.applyTarget(super.getState());
			prepare.run();
			this.binding.clear(mask);
		});
	}

	private void applyTarget(final RenderState state) {
		this.frameBufferBinding.bindFramebuffer(GlConstants.FRAMEBUFFER, state.getFrameBuffer() == null ? this.screenFrameBuffer : ((GlFrameBuffer) state.getFrameBuffer()).getId());
		this.binding.viewport(state.getViewportX(), state.getViewportY(), state.getViewportWidth(), state.getViewportHeight());
	}

	private void applyPipeline(final RenderState state) {
		final BlendState blend = state.getBlend();
		this.toggle(GlConstants.BLEND, blend.isEnabled());
		if (blend.isEnabled()) {
			this.binding.blendEquationSeparate(GlEnums.equation(blend.getEquation()), GlEnums.equation(blend.getEquation()));
			this.binding.blendFuncSeparate(GlEnums.factor(blend.getSourceColor()), GlEnums.factor(blend.getDestinationColor()), GlEnums.factor(blend.getSourceAlpha()), GlEnums.factor(blend.getDestinationAlpha()));
		}

		this.toggle(GlConstants.DEPTH_TEST, state.isDepthTest());
		this.binding.depthMask(state.isDepthWrite());
		this.toggle(GlConstants.CULL_FACE, state.isCull());
		this.binding.colorMask(state.isColorWrite(), state.isColorWrite(), state.isColorWrite(), state.isColorWrite());

		final StencilState stencil = state.getStencil();
		this.toggle(GlConstants.STENCIL_TEST, stencil.isEnabled());
		this.binding.stencilFunc(GlEnums.function(stencil.getFunction()), stencil.getReference(), stencil.getMask());
		this.binding.stencilOp(GlEnums.operation(stencil.getFail()), GlEnums.operation(stencil.getDepthFail()), GlEnums.operation(stencil.getPass()));
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
			this.binding.enableVertexAttribArray(location);
		} else {
			this.binding.disableVertexAttribArray(location);
		}
	}

}