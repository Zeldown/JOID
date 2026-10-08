package dev.joid.lib.bridge.render.shader.source;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import dev.joid.lib.bridge.render.shader.uniform.UniformBlock;
import dev.joid.lib.bridge.render.state.StencilEmulation;
import dev.joid.lib.bridge.render.vertex.VertexAttribute;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class GlslShaderTranslator {

	public static final String BLOCK           = "JoidUniforms";
	public static final String ALPHA_TEST      = "joid_AlphaTest";
	public static final String ALPHA_THRESHOLD = "joid_AlphaThreshold";

	public static final String STENCIL           = "joid_Stencil";
	public static final String STENCIL_TEST      = "joid_StencilTest";
	public static final String STENCIL_MASK      = "joid_StencilMask";
	public static final String STENCIL_FAIL      = "joid_StencilFail";
	public static final String STENCIL_PASS      = "joid_StencilPass";
	public static final String STENCIL_FUNCTION  = "joid_StencilFunction";
	public static final String STENCIL_REFERENCE = "joid_StencilReference";

	private final GlslDialect   dialect;
	private final UniformLayout layout;

	private StencilEmulation.Pass stencil;

	protected GlslShaderTranslator(final @NonNull GlslDialect dialect, final @NonNull UniformLayout layout) {
		if (layout == UniformLayout.BLOCK && !dialect.hasUniformBlocks()) {
			throw new IllegalArgumentException(dialect.getName() + " has no uniform blocks, translate its uniforms with UniformLayout.LOOSE");
		}

		this.dialect = dialect;
		this.layout  = layout;
		this.stencil = StencilEmulation.Pass.NONE;
	}

	public static @NonNull GlslShaderTranslator create(final @NonNull GlslDialect dialect, final @NonNull UniformLayout layout) {
		return new GlslShaderTranslator(dialect, layout);
	}

	@SuppressWarnings("unchecked")
	public final <T extends GlslShaderTranslator> @NonNull T stencil(final @NonNull StencilEmulation.Pass stencil) {
		this.stencil = stencil;
		return (T) this;
	}

	public final @NonNull String translateVertex(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		this.require(vertex.getFeatures());
		final StringBuilder builder = this.createHeader(vertex, fragment, vertex);
		for (final ShaderBuiltin builtin : vertex.getBuiltins()) {
			if (builtin.getKind() == ShaderBuiltin.Kind.ATTRIBUTE) {
				builder.append(this.declareAttribute(builtin, VertexAttribute.of(builtin).getLocation()));
			}
		}

		this.appendSamplers(builder, vertex, fragment, vertex);
		final List<String> varyings = GlslShaderTranslator.getVaryings(vertex, fragment);
		for (final ShaderVariable output : vertex.getOutputs()) {
			builder.append(this.declareVarying(output, varyings.indexOf(output.getName()), true));
		}
		return builder.append(this.dialect.getLineDirective()).append(vertex.getBody()).toString();
	}

	public final @NonNull String translateFragment(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		final Set<ShaderFeature> featureSet = EnumSet.noneOf(ShaderFeature.class);
		featureSet.addAll(fragment.getFeatures());
		if (this.stencil != StencilEmulation.Pass.NONE) {
			featureSet.addAll(EnumSet.of(ShaderFeature.BITWISE_OPERATORS, ShaderFeature.SWITCH, ShaderFeature.TEXEL_FETCH));
		}
		this.require(featureSet);

		final StringBuilder builder = this.createHeader(vertex, fragment, fragment).append(this.declareOutput());
		this.appendSamplers(builder, vertex, fragment, fragment);
		final List<String> varyings = GlslShaderTranslator.getVaryings(vertex, fragment);
		for (final ShaderVariable input : fragment.getInputs()) {
			builder.append(this.declareVarying(input, varyings.indexOf(input.getName()), false));
		}
		return builder.append(this.dialect.getLineDirective()).append(fragment.getBody().replaceFirst("void\\s+main\\s*\\(\\s*\\)", "void joid_main()")).append(this.getMain()).toString();
	}

	public final @NonNull UniformBlock createBlock(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		return UniformBlock.create(this.getUniforms(vertex, fragment), vertex.getBody() + fragment.getBody());
	}

	public final @NonNull List<@NonNull ShaderVariable> getUniforms(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		final List<ShaderVariable> uniformList = new ArrayList<>();
		for (final ShaderBuiltin builtin : ShaderBuiltin.values()) {
			if (builtin.getKind() == ShaderBuiltin.Kind.UNIFORM && this.isUniform(builtin) && (vertex.getBuiltins().contains(builtin) || fragment.getBuiltins().contains(builtin))) {
				uniformList.add(ShaderVariable.create(builtin.getType(), builtin.getIdentifier(), "", false));
			}
		}

		uniformList.addAll(this.getInternals(vertex, fragment));
		for (final ShaderSource source : new ShaderSource[] {vertex, fragment}) {
			for (final ShaderVariable uniform : source.getUniforms()) {
				if (uniformList.stream().noneMatch(variable -> variable.getName().equals(uniform.getName()))) {
					uniformList.add(uniform);
				}
			}
		}
		return uniformList;
	}

	public final @NonNull List<@NonNull ShaderVariable> getSamplers(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		final List<ShaderVariable> samplerList = new ArrayList<>();
		for (final ShaderSource source : new ShaderSource[] {vertex, fragment}) {
			for (final ShaderVariable sampler : source.getSamplers()) {
				if (samplerList.stream().noneMatch(variable -> variable.getName().equals(sampler.getName()))) {
					samplerList.add(sampler);
				}
			}
		}
		return samplerList;
	}

	protected boolean isUniform(final @NonNull ShaderBuiltin builtin) {
		return true;
	}

	protected @NonNull List<@NonNull ShaderVariable> getInternals(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		final List<ShaderVariable> internalList = new ArrayList<>();
		internalList.add(ShaderVariable.create("int", GlslShaderTranslator.ALPHA_TEST, "", false));
		internalList.add(ShaderVariable.create("float", GlslShaderTranslator.ALPHA_THRESHOLD, "", false));
		if (this.stencil != StencilEmulation.Pass.NONE) {
			for (final String name : new String[] {GlslShaderTranslator.STENCIL_TEST, GlslShaderTranslator.STENCIL_FUNCTION, GlslShaderTranslator.STENCIL_REFERENCE, GlslShaderTranslator.STENCIL_MASK, GlslShaderTranslator.STENCIL_FAIL, GlslShaderTranslator.STENCIL_PASS}) {
				internalList.add(ShaderVariable.create("int", name, "", false));
			}
		}
		return internalList;
	}

	protected @NonNull String getLayout() {
		return "std140";
	}

	protected @NonNull String getMain() {
		if (this.stencil == StencilEmulation.Pass.NONE) {
			return "\nvoid main() {\n\tjoid_main();\n\tif (joid_AlphaTest != 0 && fragColor.a <= joid_AlphaThreshold) {\n\t\tdiscard;\n\t}\n}\n";
		}

		return "\nuniform sampler2D " + GlslShaderTranslator.STENCIL + ";\n"
		+ "\nint joid_stencilValue() {\n\treturn int(texelFetch(joid_Stencil, ivec2(gl_FragCoord.xy), 0).r * 255.0 + 0.5);\n}\n"
		+ "\nbool joid_stencilCompare(int value) {\n\tint reference = joid_StencilReference & joid_StencilMask;\n\tint current = value & joid_StencilMask;\n\tswitch (joid_StencilFunction) {\n\tcase 0:\n\t\treturn false;\n\tcase 1:\n\t\treturn reference < current;\n\tcase 2:\n\t\treturn reference <= current;\n\tcase 3:\n\t\treturn reference > current;\n\tcase 4:\n\t\treturn reference >= current;\n\tcase 5:\n\t\treturn reference == current;\n\tcase 6:\n\t\treturn reference != current;\n\tdefault:\n\t\treturn true;\n\t}\n}\n"
		+ "\nint joid_stencilApply(int operation, int value) {\n\tswitch (operation) {\n\tcase 1:\n\t\treturn 0;\n\tcase 2:\n\t\treturn joid_StencilReference & 255;\n\tcase 3:\n\t\treturn min(value + 1, 255);\n\tcase 4:\n\t\treturn max(value - 1, 0);\n\tcase 5:\n\t\treturn ~value & 255;\n\tdefault:\n\t\treturn value;\n\t}\n}\n"
		+ "\nvoid main() {\n\tfragColor = vec4(0.0);\n\tjoid_main();\n\tif (joid_AlphaTest != 0 && fragColor.a <= joid_AlphaThreshold) {\n\t\tdiscard;\n\t}\n\n"
		+ (this.stencil == StencilEmulation.Pass.WRITE ? "\tint value = joid_stencilValue();\n\tvalue = joid_stencilApply(joid_stencilCompare(value) ? joid_StencilPass : joid_StencilFail, value);\n\tfragColor = vec4(float(value) / 255.0, 0.0, 0.0, 1.0);\n}\n" : "\tif (joid_StencilTest != 0 && !joid_stencilCompare(joid_stencilValue())) {\n\t\tdiscard;\n\t}\n}\n");
	}

	protected @NonNull String declareBuiltin(final @NonNull ShaderBuiltin builtin) {
		return "";
	}

	protected @NonNull String declareSampler(final @NonNull ShaderVariable sampler, final int unit) {
		return "uniform " + sampler.getDeclaration() + ";\n";
	}

	protected @NonNull String declareAttribute(final @NonNull ShaderBuiltin builtin, final int location) {
		final String declaration = builtin.getType() + " " + builtin.getIdentifier() + ";\n";
		if (this.dialect.hasExplicitLocations()) {
			return "layout(location = " + location + ") in " + declaration;
		}
		return (this.dialect.hasInputOutputs() ? "in " : "attribute ") + declaration;
	}

	protected @NonNull String declareVarying(final @NonNull ShaderVariable varying, final int location, final boolean output) {
		final String qualifier = this.dialect.hasInputOutputs() ? output ? "out " : "in " : "varying ";
		return (varying.isFlat() ? "flat " : "") + qualifier + varying.getDeclaration() + ";\n";
	}

	private String declareOutput() {
		if (!this.dialect.hasInputOutputs()) {
			return "#define fragColor gl_FragColor\n";
		}
		return (this.dialect.hasExplicitLocations() ? "layout(location = 0) " : "") + "out vec4 fragColor;\n";
	}

	private StringBuilder createHeader(final ShaderSource vertex, final ShaderSource fragment, final ShaderSource source) {
		final StringBuilder builder = new StringBuilder(this.dialect.getDeclaration()).append("\n\n").append(this.dialect.getPrecision());
		if (!this.dialect.hasInputOutputs()) {
			builder.append("#define texture texture2D\n");
		}

		for (final ShaderBuiltin builtin : source.getBuiltins()) {
			if (builtin.getKind() == ShaderBuiltin.Kind.UNIFORM && !this.isUniform(builtin)) {
				builder.append(this.declareBuiltin(builtin));
			}
		}

		if (this.layout == UniformLayout.LOOSE) {
			for (final ShaderVariable uniform : this.getUniforms(vertex, fragment)) {
				builder.append("uniform ").append(uniform.getDeclaration()).append(";\n");
			}
			return builder;
		}

		builder.append("layout(").append(this.getLayout()).append(") uniform ").append(GlslShaderTranslator.BLOCK).append(" {\n");
		for (final ShaderVariable uniform : this.getUniforms(vertex, fragment)) {
			builder.append('\t').append(uniform.getDeclaration()).append(";\n");
		}
		return builder.append("};\n");
	}

	private void appendSamplers(final StringBuilder builder, final ShaderSource vertex, final ShaderSource fragment, final ShaderSource source) {
		final List<ShaderVariable> samplers = this.getSamplers(vertex, fragment);
		for (final ShaderVariable sampler : source.getSamplers()) {
			for (int i = 0; i < samplers.size(); i++) {
				if (samplers.get(i).getName().equals(sampler.getName())) {
					builder.append(this.declareSampler(sampler, i + 1));
				}
			}
		}
	}

	private void require(final Set<ShaderFeature> features) {
		for (final ShaderFeature feature : features) {
			if (!this.dialect.supports(feature)) {
				throw new UnsupportedOperationException("The shader uses " + feature.getDescription() + ", which needs " + (this.dialect.isEs() ? feature.getEssl() : feature.getGlsl()).getName() + ", but the dialect is " + this.dialect.getName());
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