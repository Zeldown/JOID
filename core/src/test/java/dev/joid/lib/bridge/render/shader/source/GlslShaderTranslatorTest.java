package dev.joid.lib.bridge.render.shader.source;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.state.StencilEmulation;

public class GlslShaderTranslatorTest {

	private static final String VERTEX   = "out vec2 vTexCoord;\nflat out vec4 vColor;\nuniform float u_Scale;\nuniform vec4 u_Colors[4];\n\nvoid main() {\n    vTexCoord = aTexCoord * u_Scale;\n    vColor = aColor * u_Colors[1];\n    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);\n}\n";
	private static final String FRAGMENT = "in vec2 vTexCoord;\nflat in vec4 vColor;\nuniform sampler2D mask;\nuniform sampler2D tex;\nuniform vec3 u_Tint;\nuniform float u_Scale;\n\nvoid main() {\n    fragColor = texture(tex, vTexCoord) * texture(mask, vTexCoord) * vColor * vec4(u_Tint, uLighting ? 1.0 : 0.5);\n}\n";

	private static final String SMOOTH_VERTEX   = "out vec2 vTexCoord;\nuniform float u_Scale;\n\nvoid main() {\n    vTexCoord = aTexCoord * u_Scale;\n    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);\n}\n";
	private static final String SMOOTH_FRAGMENT = "in vec2 vTexCoord;\nuniform sampler2D tex;\nuniform vec3 u_Tint;\n\nvoid main() {\n    fragColor = texture(tex, vTexCoord) * vec4(u_Tint, 1.0);\n}\n";

	private static final String BLOCK = "layout(std140) uniform JoidUniforms {\n\tmat4 uProjectionMatrix;\n\tmat4 uModelViewMatrix;\n\tbool uLighting;\n\tint joid_AlphaTest;\n\tfloat joid_AlphaThreshold;\n\tvec4 joid_CurrentColor;\n\tint joid_VertexColor;\n\tfloat u_Scale;\n\tvec4 u_Colors[4];\n\tvec3 u_Tint;\n};\n";

	@Test
	public void sharesTheUniformBlockBetweenStages() {
		Assert.assertTrue(GlslShaderTranslatorTest.translateVertex(GlslDialect.GLSL_330).startsWith("#version 330 core\n\n" + GlslShaderTranslatorTest.BLOCK));
		Assert.assertTrue(GlslShaderTranslatorTest.translateFragment(GlslDialect.GLSL_330).startsWith("#version 330 core\n\n" + GlslShaderTranslatorTest.BLOCK));
	}

	@Test
	public void declaresTheAttributesAtTheirLocations() {
		final String vertex = GlslShaderTranslatorTest.translateVertex(GlslDialect.GLSL_330);
		Assert.assertTrue(vertex.contains("layout(location = 0) in vec3 aPosition;\n"));
		Assert.assertTrue(vertex.contains("layout(location = 1) in vec2 aTexCoord;\n"));
		Assert.assertTrue(vertex.contains("layout(location = 2) in vec4 aColor;\n"));
		Assert.assertFalse(vertex.contains("aNormal"));
	}

	@Test
	public void takesTheCurrentColorForTheVerticesWithoutColor() {
		Assert.assertTrue(GlslShaderTranslatorTest.translateVertex(GlslDialect.GLSL_330).contains("(joid_VertexColor != 0 ? aColor : joid_CurrentColor)"));
		Assert.assertFalse(GlslShaderTranslatorTest.translate(GlslDialect.GLSL_120, UniformLayout.LOOSE, GlslShaderTranslatorTest.SMOOTH_VERTEX, GlslShaderTranslatorTest.SMOOTH_FRAGMENT, true).contains("joid_CurrentColor"));
	}

	@Test
	public void declaresTheVaryingsWithTheirInterpolation() {
		Assert.assertTrue(GlslShaderTranslatorTest.translateVertex(GlslDialect.GLSL_330).contains("out vec2 vTexCoord;\nflat out vec4 vColor;\n"));
		Assert.assertTrue(GlslShaderTranslatorTest.translateFragment(GlslDialect.GLSL_330).contains("in vec2 vTexCoord;\nflat in vec4 vColor;\n"));
	}

	@Test
	public void declaresTheSamplersOfTheirStageOnly() {
		Assert.assertFalse(GlslShaderTranslatorTest.translateVertex(GlslDialect.GLSL_330).contains("sampler2D"));
		Assert.assertTrue(GlslShaderTranslatorTest.translateFragment(GlslDialect.GLSL_330).contains("layout(location = 0) out vec4 fragColor;\nuniform sampler2D mask;\nuniform sampler2D tex;\n"));
	}

	@Test
	public void wrapsTheFragmentMainWithTheAlphaTest() {
		final String fragment = GlslShaderTranslatorTest.translateFragment(GlslDialect.GLSL_330);
		Assert.assertTrue(fragment.contains("\nvoid joid_main() {\n"));
		Assert.assertTrue(fragment.endsWith("\nvoid main() {\n\tjoid_main();\n\tif (joid_AlphaTest != 0 && fragColor.a <= joid_AlphaThreshold) {\n\t\tdiscard;\n\t}\n}\n"));
	}

	@Test
	public void keepsTheLineNumbersOfTheBody() {
		for (final GlslDialect dialect : new GlslDialect[] {GlslDialect.GLSL_120, GlslDialect.GLSL_330}) {
			final String fragment = GlslShaderTranslatorTest.translate(dialect, UniformLayout.LOOSE, GlslShaderTranslatorTest.SMOOTH_VERTEX, GlslShaderTranslatorTest.SMOOTH_FRAGMENT, false);
			final String directive = dialect.getLineDirective();
			final String[] lines = fragment.substring(fragment.indexOf(directive) + directive.length()).split("\n", -1);
			Assert.assertEquals(dialect.name(), "    fragColor = texture(tex, vTexCoord) * vec4(u_Tint, 1.0);", lines[5]);
		}
	}

	@Test
	public void startsTheBodyAtLineOneInEveryDialect() {
		Assert.assertEquals("#line 0\n", GlslDialect.GLSL_120.getLineDirective());
		Assert.assertEquals("#line 0\n", GlslDialect.GLSL_150.getLineDirective());
		Assert.assertEquals("#line 0\n", GlslDialect.ESSL_100.getLineDirective());
		Assert.assertEquals("#line 1\n", GlslDialect.GLSL_330.getLineDirective());
		Assert.assertEquals("#line 1\n", GlslDialect.ESSL_300.getLineDirective());
	}

	@Test
	public void listsTheUniformsOnceForBothStages() {
		final GlslShaderTranslator translator = GlslShaderTranslator.create(GlslDialect.GLSL_330, UniformLayout.BLOCK);
		Assert.assertEquals("uProjectionMatrix uModelViewMatrix uLighting joid_AlphaTest joid_AlphaThreshold joid_CurrentColor joid_VertexColor u_Scale u_Colors u_Tint", translator.getUniforms(GlslShaderTranslatorTest.vertex(), GlslShaderTranslatorTest.fragment()).stream().map(ShaderVariable::getName).collect(Collectors.joining(" ")));
		Assert.assertEquals("mask tex", translator.getSamplers(GlslShaderTranslatorTest.vertex(), GlslShaderTranslatorTest.fragment()).stream().map(ShaderVariable::getName).collect(Collectors.joining(" ")));
	}

	@Test
	public void createsTheBlockOfTheTranslatedUniforms() {
		final GlslShaderTranslator translator = GlslShaderTranslator.create(GlslDialect.GLSL_330, UniformLayout.BLOCK);
		Assert.assertEquals(10, translator.createBlock(GlslShaderTranslatorTest.vertex(), GlslShaderTranslatorTest.fragment()).getMemberMap().size());
		Assert.assertEquals(4, translator.createBlock(GlslShaderTranslatorTest.vertex(), GlslShaderTranslatorTest.fragment()).getMember("u_Colors").getLength());
	}

	@Test
	public void declaresLooseUniformsInBothStages() {
		final String vertex = GlslShaderTranslatorTest.translate(GlslDialect.GLSL_330, UniformLayout.LOOSE, GlslShaderTranslatorTest.VERTEX, GlslShaderTranslatorTest.FRAGMENT, true);
		Assert.assertFalse(vertex.contains("JoidUniforms"));
		Assert.assertTrue(vertex.contains("uniform mat4 uProjectionMatrix;\nuniform mat4 uModelViewMatrix;\nuniform bool uLighting;\nuniform int joid_AlphaTest;\nuniform float joid_AlphaThreshold;\nuniform vec4 joid_CurrentColor;\nuniform int joid_VertexColor;\nuniform float u_Scale;\nuniform vec4 u_Colors[4];\nuniform vec3 u_Tint;\n"));
	}

	@Test
	public void writesTheOldDialectsWithAttributesAndVaryings() {
		final String vertex = GlslShaderTranslatorTest.translate(GlslDialect.GLSL_120, UniformLayout.LOOSE, GlslShaderTranslatorTest.SMOOTH_VERTEX, GlslShaderTranslatorTest.SMOOTH_FRAGMENT, true);
		final String fragment = GlslShaderTranslatorTest.translate(GlslDialect.GLSL_120, UniformLayout.LOOSE, GlslShaderTranslatorTest.SMOOTH_VERTEX, GlslShaderTranslatorTest.SMOOTH_FRAGMENT, false);
		Assert.assertTrue(vertex.startsWith("#version 120\n\n#define texture texture2D\nuniform mat4 uProjectionMatrix;\n"));
		Assert.assertTrue(vertex.contains("attribute vec3 aPosition;\nattribute vec2 aTexCoord;\nvarying vec2 vTexCoord;\n#line 0\n"));
		Assert.assertTrue(fragment.contains("#define fragColor gl_FragColor\nuniform sampler2D tex;\nvarying vec2 vTexCoord;\n#line 0\n"));
	}

	@Test
	public void writesTheInputsAndOutputsWithoutLocationsBeforeGlsl330() {
		final String vertex = GlslShaderTranslatorTest.translate(GlslDialect.GLSL_150, UniformLayout.BLOCK, GlslShaderTranslatorTest.SMOOTH_VERTEX, GlslShaderTranslatorTest.SMOOTH_FRAGMENT, true);
		final String fragment = GlslShaderTranslatorTest.translate(GlslDialect.GLSL_150, UniformLayout.BLOCK, GlslShaderTranslatorTest.SMOOTH_VERTEX, GlslShaderTranslatorTest.SMOOTH_FRAGMENT, false);
		Assert.assertTrue(vertex.startsWith("#version 150\n\nlayout(std140) uniform JoidUniforms {\n"));
		Assert.assertTrue(vertex.contains("\nin vec3 aPosition;\nin vec2 aTexCoord;\nout vec2 vTexCoord;\n#line 0\n"));
		Assert.assertTrue(fragment.contains("};\nout vec4 fragColor;\nuniform sampler2D tex;\nin vec2 vTexCoord;\n"));
	}

	@Test
	public void setsTheFloatPrecisionOfTheEsDialects() {
		Assert.assertTrue(GlslShaderTranslatorTest.translate(GlslDialect.ESSL_300, UniformLayout.BLOCK, GlslShaderTranslatorTest.SMOOTH_VERTEX, GlslShaderTranslatorTest.SMOOTH_FRAGMENT, false).startsWith("#version 300 es\n\nprecision highp float;\nprecision highp int;\nlayout(std140) uniform JoidUniforms {\n"));
		Assert.assertTrue(GlslShaderTranslatorTest.translate(GlslDialect.ESSL_100, UniformLayout.LOOSE, GlslShaderTranslatorTest.SMOOTH_VERTEX, GlslShaderTranslatorTest.SMOOTH_FRAGMENT, false).startsWith("#version 100\n\n#ifdef GL_FRAGMENT_PRECISION_HIGH\nprecision highp float;\n#else\nprecision mediump float;\n#endif\n#define texture texture2D\n"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAUniformBlockWithoutUniformBlocks() {
		GlslShaderTranslator.create(GlslDialect.GLSL_120, UniformLayout.BLOCK);
	}

	@Test
	public void refusesAShaderBeyondItsDialect() {
		final ShaderSource fragment = ShaderSource.parse(ShaderStage.FRAGMENT, "uniform uint u_Count;\n\nvoid main() {\n    fragColor = vec4(float(u_Count));\n}\n");
		try {
			GlslShaderTranslator.create(GlslDialect.GLSL_120, UniformLayout.LOOSE).translateFragment(GlslShaderTranslatorTest.vertex(), fragment);
			Assert.fail();
		} catch (final UnsupportedOperationException e) {
			Assert.assertEquals("The shader uses unsigned integers, which needs GLSL 1.30, but the dialect is GLSL 1.20", e.getMessage());
		}
		Assert.assertTrue(GlslShaderTranslator.create(GlslDialect.GLSL_130, UniformLayout.LOOSE).translateFragment(GlslShaderTranslatorTest.vertex(), fragment).contains("uniform uint u_Count;\n"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void refusesFlatVaryingsBeforeGlsl130() {
		GlslShaderTranslatorTest.translate(GlslDialect.GLSL_120, UniformLayout.LOOSE, GlslShaderTranslatorTest.VERTEX, GlslShaderTranslatorTest.FRAGMENT, true);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void refusesTheStencilEmulationBeforeGlsl130() {
		GlslShaderTranslator.create(GlslDialect.GLSL_120, UniformLayout.LOOSE).stencil(StencilEmulation.Pass.TEST).translateFragment(GlslShaderTranslatorTest.vertex(), GlslShaderTranslatorTest.smoothFragment());
	}

	@Test
	public void translatesEveryCoreShaderInEveryDialect() {
		for (final CoreShader shader : CoreShader.values()) {
			final ShaderSource vertex = shader.read(ShaderStage.VERTEX);
			final ShaderSource fragment = shader.read(ShaderStage.FRAGMENT);
			for (final GlslDialect dialect : GlslDialect.values()) {
				final GlslShaderTranslator translator = GlslShaderTranslator.create(dialect, dialect.hasUniformBlocks() ? UniformLayout.BLOCK : UniformLayout.LOOSE);
				Assert.assertTrue(shader + " " + dialect, translator.translateVertex(vertex, fragment).startsWith(dialect.getDeclaration() + "\n"));
				Assert.assertTrue(shader + " " + dialect, translator.translateFragment(vertex, fragment).startsWith(dialect.getDeclaration() + "\n"));
			}
		}
	}

	@Test
	public void writesTheIntegerLiteralsOfFloatsForGlsl110() {
		final ShaderSource vertex = ShaderSource.parse(ShaderStage.VERTEX, "void main() {\n    gl_Position = vec4(aPosition, 1);\n}\n");
		final ShaderSource fragment = ShaderSource.parse(ShaderStage.FRAGMENT, "uniform int u_Type;\n\nvoid main() {\n    fragColor = u_Type == 2 ? vec4(1) : vec4(0.5 * 2);\n}\n");
		Assert.assertTrue(GlslShaderTranslator.create(GlslDialect.GLSL_110, UniformLayout.LOOSE).translateVertex(vertex, fragment).contains("gl_Position = vec4(aPosition, 1.0);"));
		Assert.assertTrue(GlslShaderTranslator.create(GlslDialect.GLSL_110, UniformLayout.LOOSE).translateFragment(vertex, fragment).contains("fragColor = u_Type == 2 ? vec4(1.0) : vec4(0.5 * 2.0);"));
		Assert.assertTrue(GlslShaderTranslator.create(GlslDialect.ESSL_100, UniformLayout.LOOSE).translateFragment(vertex, fragment).contains("vec4(0.5 * 2.0)"));
		Assert.assertTrue(GlslShaderTranslator.create(GlslDialect.GLSL_120, UniformLayout.LOOSE).translateFragment(vertex, fragment).contains("fragColor = u_Type == 2 ? vec4(1) : vec4(0.5 * 2);"));
	}

	@Test
	public void keepsTheTranslationWithoutTheBorderEmulation() {
		final GlslShaderTranslator translator = GlslShaderTranslator.create(GlslDialect.GLSL_330, UniformLayout.BLOCK);
		Assert.assertEquals(translator.translateFragment(GlslShaderTranslatorTest.vertex(), GlslShaderTranslatorTest.fragment()), GlslShaderTranslator.create(GlslDialect.GLSL_330, UniformLayout.BLOCK).clampToBorder(false).translateFragment(GlslShaderTranslatorTest.vertex(), GlslShaderTranslatorTest.fragment()));
		Assert.assertFalse(translator.translateFragment(GlslShaderTranslatorTest.vertex(), GlslShaderTranslatorTest.fragment()).contains("joid_border"));
	}

	@Test
	public void samplesEverySamplerThroughTheBorderEmulation() {
		final String translated = GlslShaderTranslator.create(GlslDialect.GLSL_330, UniformLayout.BLOCK).clampToBorder(true).translateFragment(GlslShaderTranslatorTest.vertex(), GlslShaderTranslatorTest.fragment());
		Assert.assertTrue(translated.contains("fragColor = joid_borderTexture(tex, joid_Border_tex, vTexCoord) * joid_borderTexture(mask, joid_Border_mask, vTexCoord)"));
		Assert.assertTrue(translated.contains("\tvec3 joid_Border_mask;\n\tvec3 joid_Border_tex;\n"));
		Assert.assertTrue(translated.contains("vec4 joid_borderTexture(sampler2D joid_sampler, vec3 joid_border, vec2 joid_uv) {"));
		Assert.assertTrue(translated.contains("vec4 joid_borderTexture(sampler2D joid_sampler, vec3 joid_border, vec2 joid_uv, float joid_bias) {"));
		Assert.assertEquals(2, GlslShaderTranslatorTest.count(translated, "\\btexture\\("));
	}

	@Test
	public void declaresTheBorderOfEachSamplerOnce() {
		final GlslShaderTranslator translator = GlslShaderTranslator.create(GlslDialect.GLSL_120, UniformLayout.LOOSE).clampToBorder(true);
		final String vertex = translator.translateVertex(GlslShaderTranslatorTest.smoothVertex(), GlslShaderTranslatorTest.smoothFragment());
		Assert.assertTrue(vertex.contains("uniform vec3 joid_Border_tex;\n"));
		Assert.assertFalse(vertex.contains("joid_bias"));
		Assert.assertNotNull(translator.createBlock(GlslShaderTranslatorTest.smoothVertex(), GlslShaderTranslatorTest.smoothFragment()).getMember("joid_Border_tex"));
	}

	@Test
	public void translatesEveryCoreShaderWithTheBorderEmulationInEveryDialect() {
		for (final CoreShader shader : CoreShader.values()) {
			final ShaderSource vertex = shader.read(ShaderStage.VERTEX);
			final ShaderSource fragment = shader.read(ShaderStage.FRAGMENT);
			for (final GlslDialect dialect : GlslDialect.values()) {
				final GlslShaderTranslator translator = GlslShaderTranslator.create(dialect, dialect.hasUniformBlocks() ? UniformLayout.BLOCK : UniformLayout.LOOSE).clampToBorder(true);
				final String translated = translator.translateFragment(vertex, fragment);
				Assert.assertEquals(shader + " " + dialect, 1, GlslShaderTranslatorTest.count(translated, "\\btexture\\(joid_sampler, joid_uv\\)"));
				Assert.assertTrue(shader + " " + dialect, translator.translateVertex(vertex, fragment).startsWith(dialect.getDeclaration() + "\n"));
			}
		}
	}

	@Test
	public void declaresTheStencilUniformsInTheBlock() {
		final String block = "layout(std140) uniform JoidUniforms {\n\tmat4 uProjectionMatrix;\n\tmat4 uModelViewMatrix;\n\tint joid_AlphaTest;\n\tfloat joid_AlphaThreshold;\n\tint joid_StencilTest;\n\tint joid_StencilFunction;\n\tint joid_StencilReference;\n\tint joid_StencilMask;\n\tint joid_StencilFail;\n\tint joid_StencilPass;\n\tfloat u_Scale;\n\tvec3 u_Tint;\n};\n";
		Assert.assertTrue(GlslShaderTranslatorTest.translateStencil(StencilEmulation.Pass.TEST).contains(block));
		Assert.assertTrue(GlslShaderTranslatorTest.translateStencil(StencilEmulation.Pass.WRITE).contains(block));
	}

	@Test
	public void declaresTheStencilSamplerAfterTheShaderSamplers() {
		final String translated = GlslShaderTranslatorTest.translateStencil(StencilEmulation.Pass.TEST);
		Assert.assertTrue(translated.indexOf("uniform sampler2D tex;\n") < translated.indexOf("uniform sampler2D joid_Stencil;\n"));
		Assert.assertEquals(Collections.singletonList("tex"), GlslShaderTranslator.create(GlslDialect.GLSL_330, UniformLayout.BLOCK).stencil(StencilEmulation.Pass.TEST).getSamplers(GlslShaderTranslatorTest.smoothVertex(), GlslShaderTranslatorTest.smoothFragment()).stream().map(ShaderVariable::getName).collect(Collectors.toList()));
	}

	@Test
	public void testsTheStencilOnlyInTheTestPass() {
		Assert.assertTrue(GlslShaderTranslatorTest.translateStencil(StencilEmulation.Pass.TEST).contains("if (joid_StencilTest != 0 && !joid_stencilCompare(joid_stencilValue())) {"));
		Assert.assertFalse(GlslShaderTranslatorTest.translateStencil(StencilEmulation.Pass.TEST).contains("joid_stencilApply(joid_stencilCompare"));
	}

	@Test
	public void writesTheStencilOnlyInTheWritePass() {
		Assert.assertTrue(GlslShaderTranslatorTest.translateStencil(StencilEmulation.Pass.WRITE).contains("value = joid_stencilApply(joid_stencilCompare(value) ? joid_StencilPass : joid_StencilFail, value);"));
		Assert.assertFalse(GlslShaderTranslatorTest.translateStencil(StencilEmulation.Pass.WRITE).contains("if (joid_StencilTest != 0"));
	}

	@Test
	public void wrapsTheFragmentMainOnce() {
		for (final CoreShader shader : CoreShader.values()) {
			for (final StencilEmulation.Pass pass : StencilEmulation.Pass.values()) {
				final String translated = GlslShaderTranslator.create(GlslDialect.GLSL_330, UniformLayout.BLOCK).stencil(pass).translateFragment(shader.read(ShaderStage.VERTEX), shader.read(ShaderStage.FRAGMENT));
				Assert.assertEquals(shader.name(), 1, GlslShaderTranslatorTest.count(translated, "void\\s+joid_main\\s*\\(\\s*\\)"));
				Assert.assertEquals(shader.name(), 1, GlslShaderTranslatorTest.count(translated, "void\\s+main\\s*\\(\\s*\\)"));
				Assert.assertTrue(shader.name(), translated.contains("\tjoid_main();\n"));
			}
		}
	}

	@Test
	public void callsTheVertexMainOnBothEndsOfALine() {
		final String vertex = GlslShaderTranslator.create(GlslDialect.GLSL_330, UniformLayout.BLOCK).translateVertex(GlslShaderTranslatorTest.vertex().toLine(), GlslShaderTranslatorTest.fragment());
		Assert.assertTrue(vertex.contains("layout(location = 1) in vec2 aTexCoord;\n"));
		Assert.assertTrue(vertex.contains("layout(location = 3) in vec3 aNormal;\n"));
		Assert.assertTrue(vertex.contains("out float joid_LineAcross;\nout float joid_LineAlong;\nout float joid_LineLength;\nvec3 joid_Position;\nvec2 joid_TexCoord;\nvec3 joid_Normal;\n#line 1\n"));
		Assert.assertTrue(vertex.contains("\nvoid joid_body() {\n    vTexCoord = joid_TexCoord * u_Scale;\n"));
		Assert.assertTrue(vertex.contains("vec4(joid_Position, 1.0);"));
		Assert.assertTrue(vertex.contains("\tjoid_Position = vec3(aTexCoord, aPosition.z);\n\tjoid_body();\n\tvec4 joid_other = gl_Position;\n\tjoid_Position = aPosition;\n\tjoid_body();\n"));
		Assert.assertEquals(1, GlslShaderTranslatorTest.count(vertex, "void\\s+main\\s*\\(\\s*\\)"));
	}

	@Test
	public void coversTheFragmentsOfALine() {
		final String fragment = GlslShaderTranslator.create(GlslDialect.GLSL_330, UniformLayout.BLOCK).translateFragment(GlslShaderTranslatorTest.vertex().toLine(), GlslShaderTranslatorTest.fragment());
		Assert.assertTrue(fragment.contains("in float joid_LineAcross;\nin float joid_LineAlong;\nin float joid_LineLength;\n"));
		Assert.assertTrue(fragment.contains("\nvoid joid_body() {\n"));
		Assert.assertTrue(fragment.contains("\nvoid joid_main() {\n\tjoid_body();\n"));
		Assert.assertTrue(fragment.contains("\tfragColor = vec4(fragColor.rgb, fragColor.a * joid_across * joid_along);\n}\n\nvoid main() {\n\tjoid_main();\n"));
		Assert.assertTrue(fragment.contains("\tfloat u_Scale;\n") && fragment.contains("\tfloat joid_LineWidth;\n\tvec2 joid_LineViewport;\n"));
	}

	@Test
	public void keepsTheVaryingLocationsOfALineInBothStages() {
		final GlslShaderTranslator translator = GlslShaderTranslator.create(GlslDialect.GLSL_330, UniformLayout.BLOCK);
		final ShaderSource vertex = GlslShaderTranslatorTest.vertex().toLine();
		Assert.assertEquals(translator.getUniforms(vertex, GlslShaderTranslatorTest.fragment()).size(), translator.getUniforms(GlslShaderTranslatorTest.vertex(), GlslShaderTranslatorTest.fragment()).size() + 2);
		Assert.assertTrue(translator.translateVertex(vertex, GlslShaderTranslatorTest.fragment()).contains("flat out vec4 vColor;\nout float joid_LineAcross;"));
		Assert.assertTrue(translator.translateFragment(vertex, GlslShaderTranslatorTest.fragment()).contains("flat in vec4 vColor;\nin float joid_LineAcross;"));
	}

	@Test
	public void translatesEveryCoreShaderAsALineInEveryDialect() {
		for (final CoreShader shader : CoreShader.values()) {
			final ShaderSource vertex = shader.read(ShaderStage.VERTEX).toLine();
			final ShaderSource fragment = shader.read(ShaderStage.FRAGMENT);
			for (final GlslDialect dialect : GlslDialect.values()) {
				final GlslShaderTranslator translator = GlslShaderTranslator.create(dialect, dialect.hasUniformBlocks() ? UniformLayout.BLOCK : UniformLayout.LOOSE);
				Assert.assertTrue(shader + " " + dialect, translator.translateVertex(vertex, fragment).contains("\nvoid joid_body() {\n"));
				Assert.assertEquals(shader + " " + dialect, 1, GlslShaderTranslatorTest.count(translator.translateFragment(vertex, fragment), "void\\s+main\\s*\\(\\s*\\)"));
			}
		}
	}

	private static String translateVertex(final GlslDialect dialect) {
		return GlslShaderTranslatorTest.translate(dialect, UniformLayout.BLOCK, GlslShaderTranslatorTest.VERTEX, GlslShaderTranslatorTest.FRAGMENT, true);
	}

	private static String translateFragment(final GlslDialect dialect) {
		return GlslShaderTranslatorTest.translate(dialect, UniformLayout.BLOCK, GlslShaderTranslatorTest.VERTEX, GlslShaderTranslatorTest.FRAGMENT, false);
	}

	private static String translateStencil(final StencilEmulation.Pass pass) {
		return GlslShaderTranslator.create(GlslDialect.GLSL_330, UniformLayout.BLOCK).stencil(pass).translateFragment(GlslShaderTranslatorTest.smoothVertex(), GlslShaderTranslatorTest.smoothFragment());
	}

	private static String translate(final GlslDialect dialect, final UniformLayout layout, final String vertex, final String fragment, final boolean vertexStage) {
		final GlslShaderTranslator translator = GlslShaderTranslator.create(dialect, layout);
		final ShaderSource vertexSource = ShaderSource.parse(ShaderStage.VERTEX, vertex);
		final ShaderSource fragmentSource = ShaderSource.parse(ShaderStage.FRAGMENT, fragment);
		return vertexStage ? translator.translateVertex(vertexSource, fragmentSource) : translator.translateFragment(vertexSource, fragmentSource);
	}

	private static ShaderSource vertex() {
		return ShaderSource.parse(ShaderStage.VERTEX, GlslShaderTranslatorTest.VERTEX);
	}

	private static ShaderSource fragment() {
		return ShaderSource.parse(ShaderStage.FRAGMENT, GlslShaderTranslatorTest.FRAGMENT);
	}

	private static ShaderSource smoothVertex() {
		return ShaderSource.parse(ShaderStage.VERTEX, GlslShaderTranslatorTest.SMOOTH_VERTEX);
	}

	private static ShaderSource smoothFragment() {
		return ShaderSource.parse(ShaderStage.FRAGMENT, GlslShaderTranslatorTest.SMOOTH_FRAGMENT);
	}

	private static int count(final String text, final String regex) {
		final Matcher matcher = Pattern.compile(regex).matcher(text);
		int count = 0;
		while (matcher.find()) {
			count++;
		}
		return count;
	}

}