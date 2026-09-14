package be.zeldown.joid.impl.lwjgl3.render.shader;

import be.zeldown.joid.impl.lwjgl3.render.LWJGL3RenderBridge;
import be.zeldown.joid.lib.bridge.render.shader.source.ShaderBuiltin;
import be.zeldown.joid.lib.bridge.render.shader.source.ShaderSource;
import be.zeldown.joid.lib.bridge.render.shader.source.ShaderStage;
import be.zeldown.joid.lib.bridge.render.shader.source.ShaderVariable;
import lombok.NonNull;

public final class LWJGL3ShaderTranslator {

	public static @NonNull String translate(final @NonNull ShaderSource source) {
		final boolean vertex = source.getStage() == ShaderStage.VERTEX;
		final StringBuilder builder = new StringBuilder("#version 330 core\n\n");
		for (final ShaderBuiltin builtin : source.getBuiltins()) {
			if (builtin.getKind() == ShaderBuiltin.Kind.UNIFORM) {
				builder.append("uniform ").append(builtin.getType()).append(' ').append(builtin.getIdentifier()).append(";\n");
			} else if (builtin.getKind() == ShaderBuiltin.Kind.ATTRIBUTE && vertex) {
				builder.append("layout(location = ").append(LWJGL3ShaderTranslator.getLocation(builtin)).append(") in ").append(builtin.getType()).append(' ').append(builtin.getIdentifier()).append(";\n");
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
			return LWJGL3RenderBridge.TEXTURE_LOCATION;
		case COLOR:
			return LWJGL3RenderBridge.COLOR_LOCATION;
		case NORMAL:
			return LWJGL3RenderBridge.NORMAL_LOCATION;
		default:
			return LWJGL3RenderBridge.POSITION_LOCATION;
		}
	}

}