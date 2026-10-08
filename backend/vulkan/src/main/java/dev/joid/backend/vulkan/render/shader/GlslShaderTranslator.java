package dev.joid.backend.vulkan.render.shader;

import java.util.List;

import dev.joid.lib.bridge.render.shader.source.GlslDialect;
import dev.joid.lib.bridge.render.shader.source.ShaderBuiltin;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderVariable;
import dev.joid.lib.bridge.render.shader.source.UniformLayout;
import lombok.NonNull;

public final class GlslShaderTranslator extends dev.joid.lib.bridge.render.shader.source.GlslShaderTranslator {

	public static final String VERTEX_COLOR  = "joid_VertexColor";
	public static final String CURRENT_COLOR = "joid_CurrentColor";

	private GlslShaderTranslator() {
		super(GlslDialect.GLSL_450, UniformLayout.BLOCK);
	}

	public static @NonNull GlslShaderTranslator create() {
		return new GlslShaderTranslator();
	}

	@Override
	protected @NonNull List<@NonNull ShaderVariable> getInternals(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		final List<ShaderVariable> internalList = super.getInternals(vertex, fragment);
		if (vertex.getBuiltins().contains(ShaderBuiltin.COLOR)) {
			internalList.add(ShaderVariable.create("vec4", GlslShaderTranslator.CURRENT_COLOR, "", false));
			internalList.add(ShaderVariable.create("int", GlslShaderTranslator.VERTEX_COLOR, "", false));
		}
		return internalList;
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
	protected @NonNull String declareAttribute(final @NonNull ShaderBuiltin builtin, final int location) {
		if (builtin != ShaderBuiltin.COLOR) {
			return super.declareAttribute(builtin, location);
		}
		return "layout(location = " + location + ") in vec4 joid_Color;\n#define aColor (joid_VertexColor != 0 ? joid_Color : joid_CurrentColor)\n";
	}

	@Override
	protected @NonNull String declareVarying(final @NonNull ShaderVariable varying, final int location, final boolean output) {
		return "layout(location = " + location + ") " + super.declareVarying(varying, location, output);
	}

}