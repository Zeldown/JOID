package dev.joid.backend.vulkan.render.shader;

import dev.joid.lib.bridge.render.shader.source.GlslDialect;
import dev.joid.lib.bridge.render.shader.source.GlslShaderTranslator;
import dev.joid.lib.bridge.render.shader.source.ShaderVariable;
import dev.joid.lib.bridge.render.shader.source.UniformLayout;
import lombok.NonNull;

public final class VulkanShaderTranslator extends GlslShaderTranslator {

	private VulkanShaderTranslator() {
		super(GlslDialect.GLSL_450, UniformLayout.BLOCK);
	}

	public static @NonNull VulkanShaderTranslator create() {
		return new VulkanShaderTranslator();
	}

	@Override
	protected @NonNull String getLayout() {
		return "std140, binding = 0";
	}

	@Override
	protected @NonNull String declareSampler(final @NonNull ShaderVariable sampler, final int unit) {
		return "layout(binding = " + unit + ") " + super.declareSampler(sampler, unit);
	}

	@Override
	protected @NonNull String declareVarying(final @NonNull ShaderVariable varying, final int location, final boolean output) {
		return "layout(location = " + location + ") " + super.declareVarying(varying, location, output);
	}

}