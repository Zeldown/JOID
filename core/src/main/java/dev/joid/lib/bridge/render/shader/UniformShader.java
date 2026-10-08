package dev.joid.lib.bridge.render.shader;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.joid.lib.bridge.render.matrix.MatrixStack;
import dev.joid.lib.bridge.render.shader.source.GlslShaderTranslator;
import dev.joid.lib.bridge.render.shader.source.ShaderBuiltin;
import dev.joid.lib.bridge.render.shader.source.ShaderVariable;
import dev.joid.lib.bridge.render.shader.uniform.UniformBlock;
import dev.joid.lib.bridge.render.shader.uniform.UniformMember;
import dev.joid.lib.bridge.render.shader.uniform.UniformSampler;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class UniformShader implements IShader {

	private final UniformBlock                block;
	private final Map<String, UniformSampler> samplerMap;

	protected UniformShader(final @NonNull UniformBlock block, final @NonNull List<@NonNull ShaderVariable> samplers) {
		this.block      = block;
		this.samplerMap = new LinkedHashMap<>();
		for (final ShaderVariable sampler : samplers) {
			this.samplerMap.put(sampler.getName(), UniformSampler.create(sampler.getName(), this.samplerMap.size() + 1));
		}
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

	public final @NonNull UniformBlock builtins(final @NonNull RenderState state, final @NonNull float[] projection, final @NonNull MatrixStack modelView) {
		return this.block
		.value(ShaderBuiltin.PROJECTION_MATRIX.getIdentifier(), projection)
		.value(ShaderBuiltin.MODEL_VIEW_MATRIX.getIdentifier(), modelView.getMatrix())
		.value(ShaderBuiltin.NORMAL_MATRIX.getIdentifier(), modelView.getNormalMatrix())
		.value(ShaderBuiltin.LIGHTING.getIdentifier(), state.isLighting())
		.value(GlslShaderTranslator.ALPHA_TEST, state.isAlphaTest())
		.value(GlslShaderTranslator.ALPHA_THRESHOLD, state.getAlphaThreshold());
	}

	private UniformMember getMember(final String name) {
		final UniformMember member = this.block.getMember(name);
		if (member == null) {
			throw new IllegalArgumentException("The shader declares no uniform " + name);
		}
		return member;
	}

}