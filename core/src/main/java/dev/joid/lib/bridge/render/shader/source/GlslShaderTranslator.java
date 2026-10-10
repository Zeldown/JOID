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

	public static final String LINE_WIDTH    = "joid_LineWidth";
	public static final String LINE_VIEWPORT = "joid_LineViewport";

	public static final String VERTEX_COLOR  = "joid_VertexColor";
	public static final String CURRENT_COLOR = "joid_CurrentColor";

	public static final String BORDER = "joid_Border_";

	private static final String[] LINE_VARYINGS = {"joid_LineAcross", "joid_LineAlong", "joid_LineLength"};

	private final GlslDialect   dialect;
	private final UniformLayout layout;

	private boolean               clampToBorder;
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
	public final <T extends GlslShaderTranslator> @NonNull T clampToBorder(final boolean clampToBorder) {
		this.clampToBorder = clampToBorder;
		return (T) this;
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
		builder.append(this.getBorderFunctions(false));
		final List<String> varyings = GlslShaderTranslator.getVaryings(vertex, fragment);
		for (final ShaderVariable output : vertex.getOutputs()) {
			builder.append(this.declareVarying(output, varyings.indexOf(output.getName()), true));
		}

		if (!vertex.isLine()) {
			return builder.append(this.dialect.getLineDirective()).append(GlslShaderTranslator.colorize(this.adapt(vertex.getBody(), vertex, fragment))).toString();
		}

		this.appendLineVaryings(builder, varyings.size(), true);
		builder.append("vec3 joid_Position;\nvec2 joid_TexCoord;\nvec3 joid_Normal;\n").append(this.dialect.getLineDirective());
		final String body = GlslShaderTranslator.colorize(this.adapt(vertex.getBody(), vertex, fragment)).replaceAll("\\baPosition\\b", "joid_Position").replaceAll("\\baTexCoord\\b", "joid_TexCoord").replaceAll("\\baNormal\\b", "joid_Normal");
		return builder.append(body.replaceFirst("void\\s+main\\s*\\(\\s*\\)", "void joid_body()")).append(GlslShaderTranslator.getLineMain()).toString();
	}

	public final @NonNull String translateFragment(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		return this.translateFragment(vertex, fragment, this.stencil);
	}

	public final @NonNull String translateFragment(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull StencilEmulation.Pass stencil) {
		final Set<ShaderFeature> featureSet = EnumSet.noneOf(ShaderFeature.class);
		featureSet.addAll(fragment.getFeatures());
		if (stencil != StencilEmulation.Pass.NONE) {
			featureSet.addAll(EnumSet.of(ShaderFeature.BITWISE_OPERATORS, ShaderFeature.SWITCH, ShaderFeature.TEXEL_FETCH));
		}
		this.require(featureSet);

		final StringBuilder builder = this.createHeader(vertex, fragment, fragment).append(this.declareOutput());
		this.appendSamplers(builder, vertex, fragment, fragment);
		builder.append(this.getBorderFunctions(true));
		final List<String> varyings = GlslShaderTranslator.getVaryings(vertex, fragment);
		for (final ShaderVariable input : fragment.getInputs()) {
			builder.append(this.declareVarying(input, varyings.indexOf(input.getName()), false));
		}

		if (!vertex.isLine()) {
			return builder.append(this.dialect.getLineDirective()).append(this.adapt(fragment.getBody(), vertex, fragment).replaceFirst("void\\s+main\\s*\\(\\s*\\)", "void joid_main()")).append(this.getMain(stencil)).toString();
		}

		this.appendLineVaryings(builder, varyings.size(), false);
		builder.append(this.dialect.getLineDirective()).append(this.adapt(fragment.getBody(), vertex, fragment).replaceFirst("void\\s+main\\s*\\(\\s*\\)", "void joid_body()"));
		return builder.append("\nvoid joid_main() {\n\tjoid_body();\n\tfloat joid_across = clamp(joid_LineWidth * 0.5 + 0.5 - abs(joid_LineAcross), 0.0, 1.0);\n\tfloat joid_along = clamp(min(joid_LineAlong, joid_LineLength - joid_LineAlong) + 0.5, 0.0, 1.0);\n\tfragColor = vec4(fragColor.rgb, fragColor.a * joid_across * joid_along);\n}\n").append(this.getMain(stencil)).toString();
	}

	public final @NonNull ShaderTranslation translate(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		final String stencilFragment = this.stencil == StencilEmulation.Pass.NONE ? null : this.translateFragment(vertex, fragment, StencilEmulation.Pass.WRITE);
		return ShaderTranslation.of(this.dialect, this.translateVertex(vertex, fragment), this.translateFragment(vertex, fragment), stencilFragment);
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

	protected @NonNull String getLayout() {
		return "std140";
	}

	protected @NonNull String getMain(final @NonNull StencilEmulation.Pass stencil) {
		if (stencil == StencilEmulation.Pass.NONE) {
			return "\nvoid main() {\n\tjoid_main();\n\tif (joid_AlphaTest != 0 && fragColor.a <= joid_AlphaThreshold) {\n\t\tdiscard;\n\t}\n}\n";
		}

		return "\nuniform sampler2D " + GlslShaderTranslator.STENCIL + ";\n"
		+ "\nint joid_stencilValue() {\n\treturn int(texelFetch(joid_Stencil, ivec2(gl_FragCoord.xy), 0).r * 255.0 + 0.5);\n}\n"
		+ "\nbool joid_stencilCompare(int value) {\n\tint reference = joid_StencilReference & joid_StencilMask;\n\tint current = value & joid_StencilMask;\n\tswitch (joid_StencilFunction) {\n\tcase 0:\n\t\treturn false;\n\tcase 1:\n\t\treturn reference < current;\n\tcase 2:\n\t\treturn reference <= current;\n\tcase 3:\n\t\treturn reference > current;\n\tcase 4:\n\t\treturn reference >= current;\n\tcase 5:\n\t\treturn reference == current;\n\tcase 6:\n\t\treturn reference != current;\n\tdefault:\n\t\treturn true;\n\t}\n}\n"
		+ "\nint joid_stencilApply(int operation, int value) {\n\tswitch (operation) {\n\tcase 1:\n\t\treturn 0;\n\tcase 2:\n\t\treturn joid_StencilReference & 255;\n\tcase 3:\n\t\treturn min(value + 1, 255);\n\tcase 4:\n\t\treturn max(value - 1, 0);\n\tcase 5:\n\t\treturn ~value & 255;\n\tdefault:\n\t\treturn value;\n\t}\n}\n"
		+ "\nvoid main() {\n\tfragColor = vec4(0.0);\n\tjoid_main();\n\tif (joid_AlphaTest != 0 && fragColor.a <= joid_AlphaThreshold) {\n\t\tdiscard;\n\t}\n\n"
		+ (stencil == StencilEmulation.Pass.WRITE ? "\tint value = joid_stencilValue();\n\tvalue = joid_stencilApply(joid_stencilCompare(value) ? joid_StencilPass : joid_StencilFail, value);\n\tfragColor = vec4(float(value) / 255.0, 0.0, 0.0, 1.0);\n}\n" : "\tif (joid_StencilTest != 0 && !joid_stencilCompare(joid_stencilValue())) {\n\t\tdiscard;\n\t}\n}\n");
	}

	protected @NonNull List<@NonNull ShaderVariable> getInternals(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		final List<ShaderVariable> internalList = new ArrayList<>();
		internalList.add(ShaderVariable.create("int", GlslShaderTranslator.ALPHA_TEST, "", false));
		internalList.add(ShaderVariable.create("float", GlslShaderTranslator.ALPHA_THRESHOLD, "", false));
		if (vertex.isLine()) {
			internalList.add(ShaderVariable.create("float", GlslShaderTranslator.LINE_WIDTH, "", false));
			internalList.add(ShaderVariable.create("vec2", GlslShaderTranslator.LINE_VIEWPORT, "", false));
		}
		if (this.clampToBorder) {
			for (final ShaderVariable sampler : this.getSamplers(vertex, fragment)) {
				internalList.add(ShaderVariable.create("vec3", GlslShaderTranslator.BORDER + sampler.getName(), "", false));
			}
		}
		if (this.stencil != StencilEmulation.Pass.NONE) {
			for (final String name : new String[] {GlslShaderTranslator.STENCIL_TEST, GlslShaderTranslator.STENCIL_FUNCTION, GlslShaderTranslator.STENCIL_REFERENCE, GlslShaderTranslator.STENCIL_MASK, GlslShaderTranslator.STENCIL_FAIL, GlslShaderTranslator.STENCIL_PASS}) {
				internalList.add(ShaderVariable.create("int", name, "", false));
			}
		}
		if (vertex.getBuiltins().contains(ShaderBuiltin.COLOR)) {
			internalList.add(ShaderVariable.create("vec4", GlslShaderTranslator.CURRENT_COLOR, "", false));
			internalList.add(ShaderVariable.create("int", GlslShaderTranslator.VERTEX_COLOR, "", false));
		}
		return internalList;
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

	private String adapt(final String body, final ShaderSource vertex, final ShaderSource fragment) {
		if (this.dialect.hasImplicitConversions()) {
			return this.emulateBorder(body, vertex, fragment);
		}

		final List<ShaderVariable> variableList = new ArrayList<>(this.getUniforms(vertex, fragment));
		variableList.addAll(vertex.getOutputs());
		variableList.addAll(fragment.getInputs());
		return this.emulateBorder(ImplicitConversion.apply(body, variableList), vertex, fragment);
	}

	private String emulateBorder(final String body, final ShaderSource vertex, final ShaderSource fragment) {
		if (!this.clampToBorder) {
			return body;
		}

		String emulated = body;
		for (final ShaderVariable sampler : this.getSamplers(vertex, fragment)) {
			emulated = emulated.replaceAll("\\btexture\\s*\\(\\s*" + sampler.getName() + "\\s*,", "joid_borderTexture(" + sampler.getName() + ", " + GlslShaderTranslator.BORDER + sampler.getName() + ",");
		}
		return emulated;
	}

	private String getBorderFunctions(final boolean fragment) {
		if (!this.clampToBorder) {
			return "";
		}

		final String border = "\tif (joid_border.x < 0.5) {\n\t\treturn joid_color;\n\t}\n\tif (joid_border.x < 1.5) {\n\t\treturn any(lessThan(joid_uv, vec2(0.0))) || any(greaterThanEqual(joid_uv, vec2(1.0))) ? vec4(0.0) : joid_color;\n\t}\n\tvec2 joid_coverage = clamp(min(joid_uv, 1.0 - joid_uv) * joid_border.yz + 0.5, 0.0, 1.0);\n\treturn joid_color * joid_coverage.x * joid_coverage.y;\n}\n";
		final String sample = "\nvec4 joid_borderTexture(sampler2D joid_sampler, vec3 joid_border, vec2 joid_uv) {\n\tvec4 joid_color = texture(joid_sampler, joid_uv);\n" + border;
		return fragment ? sample + "\nvec4 joid_borderTexture(sampler2D joid_sampler, vec3 joid_border, vec2 joid_uv, float joid_bias) {\n\tvec4 joid_color = texture(joid_sampler, joid_uv, joid_bias);\n" + border : sample;
	}

	private void appendLineVaryings(final StringBuilder builder, final int location, final boolean output) {
		for (int i = 0; i < GlslShaderTranslator.LINE_VARYINGS.length; i++) {
			builder.append(this.declareVarying(ShaderVariable.create("float", GlslShaderTranslator.LINE_VARYINGS[i], "", false), location + i, output));
		}
	}

	private void require(final Set<ShaderFeature> features) {
		for (final ShaderFeature feature : features) {
			if (!this.dialect.supports(feature)) {
				throw new UnsupportedOperationException("The shader uses " + feature.getDescription() + ", which needs " + (this.dialect.isEs() ? feature.getEssl() : feature.getGlsl()).getName() + ", but the dialect is " + this.dialect.getName());
			}
		}
	}

	private static String colorize(final String body) {
		return body.replaceAll("\\baColor\\b", "(joid_VertexColor != 0 ? aColor : joid_CurrentColor)");
	}

	private static String getLineMain() {
		return "\nvoid main() {\n"
		+ "\tjoid_TexCoord = vec2(0.0);\n\tjoid_Normal = vec3(0.0, 0.0, 1.0);\n"
		+ "\tjoid_Position = vec3(aTexCoord, aPosition.z);\n\tjoid_body();\n\tvec4 joid_other = gl_Position;\n"
		+ "\tjoid_Position = aPosition;\n\tjoid_body();\n\n"
		+ "\tvec2 joid_halfViewport = joid_LineViewport * 0.5;\n"
		+ "\tvec2 joid_screen = gl_Position.xy / gl_Position.w * joid_halfViewport;\n"
		+ "\tvec2 joid_otherScreen = joid_other.xy / joid_other.w * joid_halfViewport;\n"
		+ "\tfloat joid_length = max(distance(joid_screen, joid_otherScreen), 0.0001);\n"
		+ "\tfloat joid_side = sign(aNormal.x);\n\tfloat joid_end = sign(aNormal.y);\n"
		+ "\tvec2 joid_direction = (joid_otherScreen - joid_screen) / joid_length * -joid_end;\n"
		+ "\tvec2 joid_normal = vec2(-joid_direction.y, joid_direction.x);\n"
		+ "\tfloat joid_halfWidth = joid_LineWidth * 0.5 + 1.0;\n"
		+ "\tvec2 joid_offset = joid_normal * joid_side * joid_halfWidth + joid_direction * joid_end;\n"
		+ "\tgl_Position = vec4(gl_Position.xy + joid_offset / joid_halfViewport * gl_Position.w, gl_Position.zw);\n\n"
		+ "\tjoid_LineAcross = joid_side * joid_halfWidth;\n"
		+ "\tjoid_LineAlong = joid_end < 0.0 ? -1.0 : joid_length + 1.0;\n"
		+ "\tjoid_LineLength = joid_length;\n}\n";
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