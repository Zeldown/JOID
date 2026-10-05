package dev.joid.impl.lwjgl3.render.shader;

import dev.joid.impl.lwjgl3.render.RenderBridge;
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
		final boolean vertex = source.getStage() == ShaderStage.VERTEX;
		final StringBuilder builder = new StringBuilder("#version 330 core\n\n");
		for (final ShaderBuiltin builtin : source.getBuiltins()) {
			if (builtin.getKind() == ShaderBuiltin.Kind.UNIFORM) {
				builder.append("uniform ").append(builtin.getType()).append(' ').append(builtin.getIdentifier()).append(";\n");
			} else if (builtin.getKind() == ShaderBuiltin.Kind.ATTRIBUTE && vertex) {
				builder.append("layout(location = ").append(ShaderTranslator.getLocation(builtin)).append(") in ").append(builtin.getType()).append(' ').append(builtin.getIdentifier()).append(";\n");
			}
		}

		if (!vertex) {
			builder.append("out vec4 fragColor;\n");
		}

		for (final ShaderVariable uniform : source.getUniforms()) {
			builder.append("uniform ").append(uniform.getDeclaration()).append(";\n");
		}

		for (final ShaderVariable sampler : source.getSamplers()) {
			builder.append("uniform ").append(sampler.getDeclaration()).append(";\n");
		}

		for (final ShaderVariable varying : vertex ? source.getOutputs() : source.getInputs()) {
			builder.append(varying.isFlat() ? "flat " : "").append(vertex ? "out " : "in ").append(varying.getDeclaration()).append(";\n");
		}

		return builder.append("#line 1\n").append(source.getBody()).toString();
	}

	private static int getLocation(final ShaderBuiltin builtin) {
		switch (builtin) {
		case TEXTURE_COORDINATE:
			return RenderBridge.TEXTURE_LOCATION;
		case COLOR:
			return RenderBridge.COLOR_LOCATION;
		case NORMAL:
			return RenderBridge.NORMAL_LOCATION;
		default:
			return RenderBridge.POSITION_LOCATION;
		}
	}

}