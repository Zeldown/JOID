package dev.joid.lib.bridge.render.shader;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.bridge.render.RenderBridge;
import dev.joid.lib.bridge.render.matrix.MatrixStack;
import dev.joid.lib.bridge.render.shader.source.GlslDialect;
import dev.joid.lib.bridge.render.shader.source.GlslShaderTranslator;
import dev.joid.lib.bridge.render.shader.source.ShaderBuiltin;
import dev.joid.lib.bridge.render.shader.source.ShaderFeature;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderTranslation;
import dev.joid.lib.bridge.render.shader.source.ShaderVariable;
import dev.joid.lib.bridge.render.shader.uniform.UniformBlock;
import dev.joid.lib.bridge.render.shader.uniform.UniformMember;
import dev.joid.lib.bridge.render.shader.uniform.UniformSampler;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class Shader implements IShader {

	private final BlendState   blend;
	private final RenderBridge bridge;
	private final ShaderSource vertex;
	private final ShaderSource fragment;

	private final UniformBlock                block;
	private final ShaderTranslation           translation;
	private final Map<String, UniformSampler> samplerMap;

	private boolean    bound;
	private boolean    compiled;
	private Shader     lineShader;
	private BlendState previousBlend;

	protected Shader(final @NonNull RenderBridge bridge, final @NonNull GlslShaderTranslator translator, final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull BlendState blend) {
		this.bridge     = bridge;
		this.blend      = blend;
		this.vertex     = vertex;
		this.fragment   = fragment;
		if (!bridge.canWrap(TextureWrap.CLAMP_TO_BORDER)) {
			translator.clampToBorder(true);
		}

		this.block      = translator.createBlock(vertex, fragment);
		this.samplerMap = new LinkedHashMap<>();
		for (final ShaderVariable sampler : translator.getSamplers(vertex, fragment)) {
			this.samplerMap.put(sampler.getName(), UniformSampler.create(sampler.getName(), this.samplerMap.size() + 1));
		}

		Shader.warnBeyondBaseline(vertex);
		Shader.warnBeyondBaseline(fragment);

		this.translation = translator.translate(vertex, fragment);
	}

	@Override
	public final void bind() {
		if (!this.bound) {
			this.previousBlend = this.bridge.getState().getBlend();
		}

		this.bridge.getState().shader(this).blend(this.blend);
		this.bound = true;
	}

	@Override
	public final void unbind() {
		if (this.bridge.getState().getShader() == this) {
			this.bridge.getState().shader(null);
		}

		if (this.previousBlend != null) {
			this.bridge.getState().blend(this.previousBlend);
			this.previousBlend = null;
		}

		this.bound = false;
	}

	@Override
	public final void delete() {
		if (this.lineShader != null) {
			this.lineShader.delete();
		}

		if (this.compiled) {
			this.compiled = false;
			this.deleteProgram();
		}
	}

	public final @NonNull Map<String, UniformSampler> getSamplerMap() {
		return Collections.unmodifiableMap(this.samplerMap);
	}

	@Override
	public final boolean isActive() {
		if (!this.compiled) {
			this.compiled = true;
			this.compileProgram(this.translation);
		}
		return this.isProgramActive();
	}

	@Override
	public final @NonNull IShader uniform(final @NonNull String name, final boolean value) {
		this.block.value(name, value);
		return this;
	}

	@Override
	public final @NonNull IShader uniform(final @NonNull String name, final @NonNull int... values) {
		this.block.value(name, values);
		return this;
	}

	@Override
	public final @NonNull IShader uniform(final @NonNull String name, final @NonNull float... values) {
		this.block.value(name, values);
		return this;
	}

	@Override
	public final @NonNull IShader sampler(final @NonNull String name, final @NonNull ITexture texture, final @NonNull TextureFilter filter, final @NonNull TextureWrap wrap) {
		final UniformSampler sampler = this.samplerMap.get(name);
		if (sampler == null) {
			throw new IllegalArgumentException("The shader declares no sampler " + name + ", declare it in the shader source or fix its name (samplers: " + this.samplerMap.keySet() + ")");
		}

		sampler.value(texture, filter, wrap);
		return this;
	}

	@Override
	public final @NonNull IShader getLineShader() {
		if (this.vertex.isLine()) {
			return this;
		}

		if (this.lineShader == null) {
			this.lineShader = (Shader) this.bridge.createShader(this.vertex.toLine(), this.fragment, this.blend);
		}

		for (final UniformMember member : this.block.getMemberMap().values()) {
			this.lineShader.getBlock().getMember(member.getName()).value(member);
		}
		for (final UniformSampler sampler : this.samplerMap.values()) {
			if (sampler.getTexture() != null) {
				this.lineShader.samplerMap.get(sampler.getName()).value(sampler.getTexture(), sampler.getFilter(), sampler.getWrap());
			}
		}
		return this.lineShader;
	}

	public final @NonNull UniformBlock builtins(final @NonNull RenderState state, final @NonNull VertexBuffer buffer, final @NonNull float[] projection, final @NonNull MatrixStack modelView) {
		for (final UniformSampler sampler : this.samplerMap.values()) {
			final String name = GlslShaderTranslator.BORDER + sampler.getName();
			if (this.block.getMember(name) != null) {
				this.block.value(name, Shader.getBorder(sampler, state));
			}
		}

		this.builtin(ShaderBuiltin.PROJECTION_MATRIX.getIdentifier(), projection);
		this.builtin(ShaderBuiltin.MODEL_VIEW_MATRIX.getIdentifier(), modelView.getRawMatrix());
		this.builtin(ShaderBuiltin.NORMAL_MATRIX.getIdentifier(), modelView.getNormalMatrix());
		this.builtin(ShaderBuiltin.LIGHTING.getIdentifier(), state.isLighting());
		this.builtin(GlslShaderTranslator.ALPHA_TEST, state.getAlphaCutoff() > 0F);
		this.builtin(GlslShaderTranslator.ALPHA_THRESHOLD, state.getAlphaCutoff());
		this.builtin(GlslShaderTranslator.LINE_WIDTH, state.getLineWidth());
		this.builtin(GlslShaderTranslator.LINE_VIEWPORT, state.getViewportWidth(), state.getViewportHeight());
		this.builtin(GlslShaderTranslator.VERTEX_COLOR, buffer.isColor());
		this.builtin(GlslShaderTranslator.CURRENT_COLOR, state.getRed(), state.getGreen(), state.getBlue(), state.getAlpha());
		return this.block;
	}

	protected abstract boolean isProgramActive();

	protected abstract void compileProgram(final @NonNull ShaderTranslation translation);
	protected abstract void deleteProgram();

	private void builtin(final String name, final boolean value) {
		final UniformMember member = this.block.getMember(name);
		if (member != null) {
			member.value(value);
		}
	}

	private void builtin(final String name, final float... values) {
		final UniformMember member = this.block.getMember(name);
		if (member != null) {
			member.value(values);
		}
	}

	private static void warnBeyondBaseline(final ShaderSource source) {
		for (final ShaderFeature feature : source.getFeatures()) {
			if (!GlslDialect.GLSL_120.supports(feature)) {
				JOIDLog.devWarnOnce("Shader.baseline." + feature.name() + source.getBody(), () -> "A " + source.getStage().name().toLowerCase(Locale.ROOT) + " shader uses " + feature.getDescription() + ", which needs " + feature.getGlsl().getName() + ": the OpenGL 2.1 contexts (GLSL 1.20) that JOID supports refuse it, keep to GLSL 1.20 to draw on every backend");
			}
		}
	}

	private static float[] getBorder(final UniformSampler sampler, final RenderState state) {
		final boolean own = sampler.getTexture() != null && sampler.getTexture().isAllocated();
		final ITexture texture = own ? sampler.getTexture() : state.getTexture();
		final TextureWrap wrap = own ? sampler.getWrap() : state.getTextureWrap();
		if (texture == null || !texture.isAllocated() || wrap != TextureWrap.CLAMP_TO_BORDER) {
			return new float[] {0F, 1F, 1F};
		}
		return new float[] {(own ? sampler.getFilter() : state.getTextureFilter()) == TextureFilter.LINEAR ? 2F : 1F, texture.getWidth(), texture.getHeight()};
	}

}