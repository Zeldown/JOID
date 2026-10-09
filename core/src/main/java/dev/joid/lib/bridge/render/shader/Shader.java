package dev.joid.lib.bridge.render.shader;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.joid.lib.bridge.render.RenderBridge;
import dev.joid.lib.bridge.render.matrix.MatrixStack;
import dev.joid.lib.bridge.render.shader.source.GlslShaderTranslator;
import dev.joid.lib.bridge.render.shader.source.ShaderBuiltin;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderVariable;
import dev.joid.lib.bridge.render.shader.uniform.UniformBlock;
import dev.joid.lib.bridge.render.shader.uniform.UniformMember;
import dev.joid.lib.bridge.render.shader.uniform.UniformSampler;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class Shader implements IShader {

	private final RenderBridge bridge;
	private final BlendState   blend;
	private final boolean      active;
	private final ShaderSource vertex;
	private final ShaderSource fragment;

	private final UniformBlock                block;
	private final Map<String, UniformSampler> samplerMap;

	private boolean    bound;
	private Shader     lineShader;
	private BlendState previousBlend;

	protected Shader(final @NonNull RenderBridge bridge, final @NonNull GlslShaderTranslator translator, final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull BlendState blend, final boolean active) {
		this.bridge     = bridge;
		this.blend      = blend;
		this.active     = active;
		this.vertex     = vertex;
		this.fragment   = fragment;
		this.block      = translator.createBlock(vertex, fragment);
		this.samplerMap = new LinkedHashMap<>();
		for (final ShaderVariable sampler : translator.getSamplers(vertex, fragment)) {
			this.samplerMap.put(sampler.getName(), UniformSampler.create(sampler.getName(), this.samplerMap.size() + 1));
		}
	}

	@Override
	public final void bind() {
		this.previousBlend = this.bridge.getState().getBlend();
		this.bridge.shader(this);
		this.bridge.blend(this.blend);
		this.bound = true;
	}

	@Override
	public final void unbind() {
		this.bridge.shader(null);
		if (this.previousBlend != null) {
			this.bridge.blend(this.previousBlend);
			this.previousBlend = null;
		}

		this.bound = false;
	}

	@Override
	public final @NonNull IShader uniform(final @NonNull String name, final int value) {
		this.getMember(name).value(value);
		return this;
	}

	@Override
	public final @NonNull IShader uniform(final @NonNull String name, final boolean value) {
		this.getMember(name).value(value);
		return this;
	}

	@Override
	public final @NonNull IShader uniform(final @NonNull String name, final @NonNull float... values) {
		this.getMember(name).value(values);
		return this;
	}

	@Override
	public final @NonNull IShader sampler(final @NonNull String name, final @NonNull ITexture texture, final @NonNull TextureFilter filter, final @NonNull TextureWrap wrap) {
		final UniformSampler sampler = this.samplerMap.get(name);
		if (sampler == null) {
			throw new IllegalArgumentException("The shader declares no sampler " + name);
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
				this.lineShader.getSamplerMap().get(sampler.getName()).value(sampler.getTexture(), sampler.getFilter(), sampler.getWrap());
			}
		}
		return this.lineShader;
	}

	public final @NonNull UniformBlock builtins(final @NonNull RenderState state, final @NonNull float[] projection, final @NonNull MatrixStack modelView) {
		return this.block
		.value(ShaderBuiltin.PROJECTION_MATRIX.getIdentifier(), projection)
		.value(ShaderBuiltin.MODEL_VIEW_MATRIX.getIdentifier(), modelView.getMatrix())
		.value(ShaderBuiltin.NORMAL_MATRIX.getIdentifier(), modelView.getNormalMatrix())
		.value(ShaderBuiltin.LIGHTING.getIdentifier(), state.isLighting())
		.value(GlslShaderTranslator.ALPHA_TEST, state.isAlphaTest())
		.value(GlslShaderTranslator.ALPHA_THRESHOLD, state.getAlphaThreshold())
		.value(GlslShaderTranslator.LINE_WIDTH, state.getLineWidth())
		.value(GlslShaderTranslator.LINE_VIEWPORT, state.getViewportWidth(), state.getViewportHeight());
	}

	private UniformMember getMember(final String name) {
		final UniformMember member = this.block.getMember(name);
		if (member == null) {
			throw new IllegalArgumentException("The shader declares no uniform " + name);
		}
		return member;
	}

}