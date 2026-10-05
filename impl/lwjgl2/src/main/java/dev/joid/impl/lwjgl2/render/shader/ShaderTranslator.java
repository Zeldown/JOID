package dev.joid.impl.lwjgl2.render.shader;

import dev.joid.lib.bridge.render.shader.source.ShaderBuiltin;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.bridge.render.shader.source.ShaderVariable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderTranslator {

	public static @NonNull String translate(final @NonNull ShaderSource source) {
		final StringBuilder builder = new StringBuilder("#version 120\n\n#define texture texture2D\n");
		for (final ShaderBuiltin builtin : source.getBuiltins()) {
			if (builtin == ShaderBuiltin.LIGHTING) {
				builder.append("uniform ").append(builtin.getType()).append(' ').append(builtin.getIdentifier()).append(";\n");
			} else {
				builder.append("#define ").append(builtin.getIdentifier()).append(' ').append(ShaderTranslator.getBuiltin(builtin)).append('\n');
			}
		}

		for (final ShaderVariable uniform : source.getUniforms()) {
			builder.append("uniform ").append(uniform.getDeclaration()).append(";\n");
		}

		for (final ShaderVariable sampler : source.getSamplers()) {
			builder.append("uniform ").append(sampler.getDeclaration()).append(";\n");
		}

		for (final ShaderVariable varying : source.getStage() == ShaderStage.VERTEX ? source.getOutputs() : source.getInputs()) {
			builder.append("varying ").append(varying.getDeclaration()).append(";\n");
		}

		return builder.append("#line 0\n").append(source.getBody()).toString();
	}

	private static String getBuiltin(final ShaderBuiltin builtin) {
		switch (builtin) {
		case POSITION:
			return "gl_Vertex.xyz";
		case TEXTURE_COORDINATE:
			return "gl_MultiTexCoord0.xy";
		case COLOR:
			return "gl_Color";
		case NORMAL:
			return "gl_Normal";
		case PROJECTION_MATRIX:
			return "gl_ProjectionMatrix";
		case MODEL_VIEW_MATRIX:
			return "gl_ModelViewMatrix";
		case NORMAL_MATRIX:
			return "gl_NormalMatrix";
		default:
			return "gl_FragColor";
		}
	}

}