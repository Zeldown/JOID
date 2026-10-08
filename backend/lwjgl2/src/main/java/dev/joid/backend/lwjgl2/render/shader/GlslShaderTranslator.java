package dev.joid.backend.lwjgl2.render.shader;

import dev.joid.lib.bridge.render.shader.source.GlslDialect;
import dev.joid.lib.bridge.render.shader.source.ShaderBuiltin;
import dev.joid.lib.bridge.render.shader.source.UniformLayout;
import lombok.NonNull;

public final class GlslShaderTranslator extends dev.joid.lib.bridge.render.shader.source.GlslShaderTranslator {

	private GlslShaderTranslator() {
		super(GlslDialect.GLSL_120, UniformLayout.LOOSE);
	}

	public static @NonNull GlslShaderTranslator create() {
		return new GlslShaderTranslator();
	}

	@Override
	protected boolean isUniform(final @NonNull ShaderBuiltin builtin) {
		return builtin == ShaderBuiltin.LIGHTING;
	}

	@Override
	protected @NonNull String declareBuiltin(final @NonNull ShaderBuiltin builtin) {
		return "#define " + builtin.getIdentifier() + " " + GlslShaderTranslator.getFixedName(builtin) + "\n";
	}

	@Override
	protected @NonNull String declareAttribute(final @NonNull ShaderBuiltin builtin, final int location) {
		if (builtin == ShaderBuiltin.NORMAL) {
			return "attribute " + builtin.getType() + " joid_Normal;\n#define " + builtin.getIdentifier() + " (joid_Normal / 127.0)\n";
		}
		return "#define " + builtin.getIdentifier() + " " + GlslShaderTranslator.getFixedName(builtin) + "\n";
	}

	private static String getFixedName(final ShaderBuiltin builtin) {
		switch (builtin) {
		case POSITION:
			return "gl_Vertex.xyz";
		case TEXTURE_COORDINATE:
			return "gl_MultiTexCoord0.xy";
		case COLOR:
			return "gl_Color";
		case PROJECTION_MATRIX:
			return "gl_ProjectionMatrix";
		case MODEL_VIEW_MATRIX:
			return "gl_ModelViewMatrix";
		default:
			return "gl_NormalMatrix";
		}
	}

}