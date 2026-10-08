package dev.joid.base.opengl.render;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.binding.IGlBufferBinding;
import dev.joid.base.opengl.binding.IGlStateBinding;
import dev.joid.base.opengl.binding.IGlTextureBinding;
import dev.joid.base.opengl.capability.GlCapabilities;
import dev.joid.base.opengl.capability.GlStrategies;
import dev.joid.base.opengl.render.framebuffer.GlFrameBuffer;
import dev.joid.base.opengl.render.shader.GlShader;
import dev.joid.base.opengl.render.texture.GlTexture;
import dev.joid.lib.bridge.render.RenderBridge;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.SamplerBinding;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.uniform.UniformSampler;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureSampling;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.bridge.render.vertex.VertexAttribute;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class GlRenderBridge extends RenderBridge {

	private final IGlBinding     binding;
	private final int[]          samplers;
	private final int            vertexArray;
	private final int            vertexBuffer;
	private final GlStrategies   strategies;
	private final GlCapabilities capabilities;

	protected GlRenderBridge(final @NonNull IGlBinding binding) {
		this.binding      = binding;
		this.capabilities = GlCapabilities.read(binding);
		this.strategies   = GlStrategies.of(this.capabilities);
		this.samplers     = this.createSamplers();

		final IGlBufferBinding buffer = binding.getBufferBinding();
		this.vertexArray  = buffer.genVertexArray();
		this.vertexBuffer = buffer.genBuffer();
		buffer.bindVertexArray(this.vertexArray);
		buffer.bindBuffer(GlConstants.ARRAY_BUFFER, this.vertexBuffer);
		for (final VertexAttribute attribute : VertexAttribute.values()) {
			buffer.vertexAttribPointer(attribute.getLocation(), attribute.getComponents(), GlEnums.type(attribute.getComponent()), attribute.isNormalized(), VertexBuffer.STRIDE, attribute.getOffset());
		}
		buffer.enableVertexAttribArray(VertexAttribute.POSITION.getLocation());
	}

	public static @NonNull GlRenderBridge create(final @NonNull IGlBinding binding) {
		return new GlRenderBridge(binding);
	}

	@Override
	public void clear(final float red, final float green, final float blue, final float alpha) {
		final RenderState state = super.getState();
		this.applyTarget(state);
		this.binding.getStateBinding().colorMask(state.isColorMask(), state.isColorMask(), state.isColorMask(), state.isColorMask());
		this.binding.getStateBinding().clearColor(red, green, blue, alpha);
		this.binding.getFrameBufferBinding().clear(GlConstants.COLOR_BUFFER_BIT);
	}

	@Override
	public void clearDepth() {
		this.applyTarget(super.getState());
		this.binding.getStateBinding().depthMask(true);
		this.binding.getFrameBufferBinding().clear(GlConstants.DEPTH_BUFFER_BIT);
		this.binding.getStateBinding().depthMask(super.getState().isDepthWrite());
	}

	@Override
	public void clearStencil() {
		this.applyTarget(super.getState());
		this.binding.getStateBinding().stencilMask(0xFF);
		this.binding.getFrameBufferBinding().clear(GlConstants.STENCIL_BUFFER_BIT);
	}

	@Override
	protected void drawPrimitive(final @NonNull Primitive primitive, final @NonNull VertexBuffer buffer, final @NonNull IShader current) {
		final RenderState state = super.getState();
		this.applyTarget(state);
		this.applyPipeline(state);
		final GlShader shader = (GlShader) current;
		shader.use(state);
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
		vertices.bindVertexArray(this.vertexArray);
		vertices.bindBuffer(GlConstants.ARRAY_BUFFER, this.vertexBuffer);
		vertices.bufferData(GlConstants.ARRAY_BUFFER, buffer.getBuffer(), GlConstants.STREAM_DRAW);
		this.toggleAttribute(VertexAttribute.TEXTURE_COORDINATE.getLocation(), buffer.isTexture());
		this.toggleAttribute(VertexAttribute.COLOR.getLocation(), buffer.isColor());
		this.toggleAttribute(VertexAttribute.NORMAL.getLocation(), buffer.isNormal());
		vertices.vertexAttrib2f(VertexAttribute.TEXTURE_COORDINATE.getLocation(), 0F, 0F);
		vertices.vertexAttrib4f(VertexAttribute.COLOR.getLocation(), state.getRed(), state.getGreen(), state.getBlue(), state.getAlpha());
		vertices.vertexAttrib3f(VertexAttribute.NORMAL.getLocation(), 0F, 0F, 1F);
		vertices.drawArrays(GlEnums.mode(primitive), 0, buffer.getCount());
	}

	@Override
	public @NonNull ITexture createTexture() {
		return GlTexture.create(this.binding);
	}

	@Override
	public @NonNull IFrameBuffer createFrameBuffer(final int width, final int height) {
		return GlFrameBuffer.create(this.binding, width, height);
	}

	@Override
	public @NonNull IShader createShader(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull BlendState blend) {
		return GlShader.create(this, vertex, fragment, blend);
	}

	private void bindTexture(final int unit, final SamplerBinding resolved) {
		final IGlTextureBinding texture = this.binding.getTextureBinding();
		texture.activeTexture(GlConstants.TEXTURE0 + unit);
		texture.bindTexture(GlConstants.TEXTURE_2D, ((GlTexture) resolved.getTexture()).getId());
		texture.bindSampler(unit, this.samplers[resolved.getSampling().getIndex()]);
	}

	private void applyTarget(final RenderState state) {
		this.binding.getFrameBufferBinding().bindFramebuffer(GlConstants.FRAMEBUFFER, state.getFrameBuffer() == null ? 0 : ((GlFrameBuffer) state.getFrameBuffer()).getId());
		this.binding.getStateBinding().viewport(state.getViewportX(), state.getViewportY(), state.getViewportWidth(), state.getViewportHeight());
	}

	private void applyPipeline(final RenderState state) {
		final IGlStateBinding pipeline = this.binding.getStateBinding();
		final BlendState blend = state.getBlend();
		this.toggle(GlConstants.BLEND, blend.isEnabled());
		if (blend.isEnabled()) {
			pipeline.blendEquation(GlEnums.equation(blend.getEquation()));
			pipeline.blendFuncSeparate(GlEnums.factor(blend.getSourceColor()), GlEnums.factor(blend.getDestinationColor()), GlEnums.factor(blend.getSourceAlpha()), GlEnums.factor(blend.getDestinationAlpha()));
		}

		this.toggle(GlConstants.DEPTH_TEST, state.isDepthTest());
		pipeline.depthMask(state.isDepthWrite());
		this.toggle(GlConstants.CULL_FACE, state.isCull());
		pipeline.colorMask(state.isColorMask(), state.isColorMask(), state.isColorMask(), state.isColorMask());

		this.toggle(GlConstants.STENCIL_TEST, state.isStencilTest());
		pipeline.stencilFunc(GlEnums.function(state.getStencilFunction()), state.getStencilReference(), state.getStencilMask());
		pipeline.stencilOp(GlEnums.operation(state.getStencilFail()), GlEnums.operation(state.getStencilDepthFail()), GlEnums.operation(state.getStencilPass()));

		final float[] lineWidthRange = state.isLineSmooth() ? this.capabilities.getSmoothLineWidthRange() : this.capabilities.getAliasedLineWidthRange();
		pipeline.lineWidth(Math.max(lineWidthRange[0], Math.min(lineWidthRange[1], state.getLineWidth())));
		this.toggle(GlConstants.LINE_SMOOTH, state.isLineSmooth());
	}

	private int[] createSamplers() {
		final IGlTextureBinding texture = this.binding.getTextureBinding();
		final int[] samplers = new int[TextureSampling.values().size()];
		for (final TextureSampling sampling : TextureSampling.values()) {
			final int sampler = texture.genSampler();
			texture.samplerParameteri(sampler, GlConstants.TEXTURE_MIN_FILTER, GlEnums.minFilter(sampling));
			texture.samplerParameteri(sampler, GlConstants.TEXTURE_MAG_FILTER, GlEnums.magFilter(sampling));
			texture.samplerParameteri(sampler, GlConstants.TEXTURE_WRAP_S, GlEnums.wrap(sampling.getWrap()));
			texture.samplerParameteri(sampler, GlConstants.TEXTURE_WRAP_T, GlEnums.wrap(sampling.getWrap()));
			samplers[sampling.getIndex()] = sampler;
		}
		return samplers;
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