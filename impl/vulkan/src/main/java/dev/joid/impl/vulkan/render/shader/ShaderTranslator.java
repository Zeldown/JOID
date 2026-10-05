package dev.joid.impl.vulkan.render.shader;

import java.util.ArrayList;
import java.util.List;

import dev.joid.lib.bridge.render.shader.source.ShaderBuiltin;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.bridge.render.shader.source.ShaderVariable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderTranslator {

	public static @NonNull String translateVertex(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		return ShaderTranslator.translate(vertex, vertex, fragment);
	}

	public static @NonNull String translateFragment(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		return ShaderTranslator.translate(fragment, vertex, fragment);
	}

	private static String translate(final ShaderSource source, final ShaderSource vertex, final ShaderSource fragment) {
		final boolean vertexStage = source.getStage() == ShaderStage.VERTEX;
		final StringBuilder builder = new StringBuilder("#version 450\n\n");
		if (vertexStage) {
			for (final ShaderBuiltin builtin : source.getBuiltins()) {
				if (builtin.getKind() == ShaderBuiltin.Kind.ATTRIBUTE) {
					builder.append("layout(location = ").append(ShaderTranslator.getLocation(builtin)).append(") in ").append(builtin.getType()).append(' ').append(builtin == ShaderBuiltin.COLOR ? "joid_Color" : builtin.getIdentifier()).append(";\n");
				}
			}
		} else {
			builder.append("layout(location = 0) out vec4 fragColor;\n");
		}

		final StringBuilder block = new StringBuilder();
		for (final ShaderBuiltin builtin : ShaderBuiltin.values()) {
			if (builtin.getKind() == ShaderBuiltin.Kind.UNIFORM && (vertex.getBuiltins().contains(builtin) || fragment.getBuiltins().contains(builtin))) {
				block.append('\t').append(builtin.getType()).append(' ').append(builtin.getIdentifier()).append(";\n");
			}
		}

		if (vertex.getBuiltins().contains(ShaderBuiltin.COLOR)) {
			block.append("\tvec4 joid_CurrentColor;\n\tint joid_VertexColor;\n");
		}

		final List<String> uniformNames = new ArrayList<>();
		final List<String> samplerNames = new ArrayList<>();
		for (final ShaderSource shader : new ShaderSource[] {vertex, fragment}) {
			for (final ShaderVariable uniform : shader.getUniforms()) {
				if (!uniformNames.contains(uniform.getName())) {
					uniformNames.add(uniform.getName());
					block.append('\t').append(uniform.getDeclaration()).append(";\n");
				}
			}

			for (final ShaderVariable sampler : shader.getSamplers()) {
				if (!samplerNames.contains(sampler.getName())) {
					samplerNames.add(sampler.getName());
				}
			}
		}

		if (block.length() > 0) {
			builder.append("layout(std140, binding = 0) uniform JoidUniforms {\n").append(block).append("};\n");
		}

		for (final ShaderVariable sampler : source.getSamplers()) {
			builder.append("layout(binding = ").append(samplerNames.indexOf(sampler.getName()) + 1).append(") uniform ").append(sampler.getDeclaration()).append(";\n");
		}

		final List<String> varyingNames = new ArrayList<>();
		for (final ShaderVariable output : vertex.getOutputs()) {
			varyingNames.add(output.getName());
		}

		for (final ShaderVariable varying : vertexStage ? source.getOutputs() : source.getInputs()) {
			if (!varyingNames.contains(varying.getName())) {
				varyingNames.add(varying.getName());
			}
			builder.append("layout(location = ").append(varyingNames.indexOf(varying.getName())).append(") ").append(varying.isFlat() ? "flat " : "").append(vertexStage ? "out " : "in ").append(varying.getDeclaration()).append(";\n");
		}

		if (vertexStage && source.getBuiltins().contains(ShaderBuiltin.COLOR)) {
			builder.append("#define aColor (joid_VertexColor != 0 ? joid_Color : joid_CurrentColor)\n");
		}

		return builder.append("#line 1\n").append(source.getBody()).toString();
	}

	private static int getLocation(final ShaderBuiltin builtin) {
		switch (builtin) {
		case TEXTURE_COORDINATE:
			return 1;
		case COLOR:
			return 2;
		case NORMAL:
			return 3;
		default:
			return 0;
		}
	}

}