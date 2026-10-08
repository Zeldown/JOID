package dev.joid.lib.bridge.render.shader.source;

import java.util.ArrayList;
import java.util.List;

import dev.joid.lib.bridge.render.vertex.VertexAttribute;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockShaderTranslator extends ShaderTranslator {

	public static final String BLOCK           = "JoidUniforms";
	public static final String ALPHA_TEST      = "joid_AlphaTest";
	public static final String ALPHA_THRESHOLD = "joid_AlphaThreshold";

	public static @NonNull BlockShaderTranslator create() {
		return new BlockShaderTranslator();
	}

	@Override
	public @NonNull String translateVertex(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		final StringBuilder builder = this.createHeader(vertex, fragment);
		for (final ShaderBuiltin builtin : vertex.getBuiltins()) {
			if (builtin.getKind() == ShaderBuiltin.Kind.ATTRIBUTE) {
				builder.append(this.declareAttribute(builtin, VertexAttribute.of(builtin).getLocation()));
			}
		}

		this.appendSamplers(builder, vertex, fragment, vertex);
		final List<String> varyings = BlockShaderTranslator.getVaryings(vertex, fragment);
		for (final ShaderVariable output : vertex.getOutputs()) {
			builder.append(this.declareVarying(output, varyings.indexOf(output.getName()), true));
		}
		return builder.append("#line 1\n").append(vertex.getBody()).toString();
	}

	@Override
	public @NonNull String translateFragment(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		final StringBuilder builder = this.createHeader(vertex, fragment).append("layout(location = 0) out vec4 fragColor;\n");
		this.appendSamplers(builder, vertex, fragment, fragment);
		final List<String> varyings = BlockShaderTranslator.getVaryings(vertex, fragment);
		for (final ShaderVariable input : fragment.getInputs()) {
			builder.append(this.declareVarying(input, varyings.indexOf(input.getName()), false));
		}
		return builder.append("#line 1\n").append(fragment.getBody().replaceFirst("void\\s+main\\s*\\(\\s*\\)", "void joid_main()")).append(this.getMain()).toString();
	}

	protected @NonNull String getLayout() {
		return "std140";
	}

	protected @NonNull String getVersion() {
		return "#version 330 core";
	}

	protected @NonNull String getMain() {
		return "\nvoid main() {\n\tjoid_main();\n\tif (joid_AlphaTest != 0 && fragColor.a <= joid_AlphaThreshold) {\n\t\tdiscard;\n\t}\n}\n";
	}

	@Override
	protected @NonNull List<@NonNull ShaderVariable> getInternals(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		final List<ShaderVariable> internalList = super.getInternals(vertex, fragment);
		internalList.add(ShaderVariable.create("int", BlockShaderTranslator.ALPHA_TEST, "", false));
		internalList.add(ShaderVariable.create("float", BlockShaderTranslator.ALPHA_THRESHOLD, "", false));
		return internalList;
	}

	protected @NonNull String declareSampler(final @NonNull ShaderVariable sampler, final int unit) {
		return "uniform " + sampler.getDeclaration() + ";\n";
	}

	protected @NonNull String declareAttribute(final @NonNull ShaderBuiltin builtin, final int location) {
		return "layout(location = " + location + ") in " + builtin.getType() + " " + builtin.getIdentifier() + ";\n";
	}

	protected @NonNull String declareVarying(final @NonNull ShaderVariable varying, final int location, final boolean output) {
		return (varying.isFlat() ? "flat " : "") + (output ? "out " : "in ") + varying.getDeclaration() + ";\n";
	}

	private StringBuilder createHeader(final ShaderSource vertex, final ShaderSource fragment) {
		final StringBuilder builder = new StringBuilder(this.getVersion()).append("\n\nlayout(").append(this.getLayout()).append(") uniform ").append(BlockShaderTranslator.BLOCK).append(" {\n");
		for (final ShaderVariable uniform : super.getUniforms(vertex, fragment)) {
			builder.append('\t').append(uniform.getDeclaration()).append(";\n");
		}
		return builder.append("};\n");
	}

	private void appendSamplers(final StringBuilder builder, final ShaderSource vertex, final ShaderSource fragment, final ShaderSource source) {
		final List<ShaderVariable> samplers = super.getSamplers(vertex, fragment);
		for (final ShaderVariable sampler : source.getSamplers()) {
			for (int i = 0; i < samplers.size(); i++) {
				if (samplers.get(i).getName().equals(sampler.getName())) {
					builder.append(this.declareSampler(sampler, i + 1));
				}
			}
		}
	}

	private static List<String> getVaryings(final ShaderSource vertex, final ShaderSource fragment) {
		final List<String> varyingList = new ArrayList<>();
		for (final ShaderSource source : new ShaderSource[] {vertex, fragment}) {
			for (final ShaderVariable varying : source == vertex ? source.getOutputs() : source.getInputs()) {
				if (!varyingList.contains(varying.getName())) {
					varyingList.add(varying.getName());
				}
			}
		}
		return varyingList;
	}

}