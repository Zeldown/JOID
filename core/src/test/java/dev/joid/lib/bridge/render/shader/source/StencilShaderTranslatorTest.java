package dev.joid.lib.bridge.render.shader.source;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class StencilShaderTranslatorTest {

	private static final String VERTEX   = "out vec2 vTexCoord;\nuniform float u_Scale;\n\nvoid main() {\n    vTexCoord = aTexCoord * u_Scale;\n    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);\n}\n";
	private static final String FRAGMENT = "in vec2 vTexCoord;\nuniform sampler2D tex;\nuniform vec3 u_Tint;\n\nvoid main() {\n    fragColor = texture(tex, vTexCoord) * vec4(u_Tint, 1.0);\n}\n";

	@Test
	public void declaresTheStencilUniformsInTheBlock() {
		final String block = "layout(std140) uniform JoidUniforms {\n\tmat4 uProjectionMatrix;\n\tmat4 uModelViewMatrix;\n\tint joid_AlphaTest;\n\tfloat joid_AlphaThreshold;\n\tint joid_StencilTest;\n\tint joid_StencilFunction;\n\tint joid_StencilReference;\n\tint joid_StencilMask;\n\tint joid_StencilFail;\n\tint joid_StencilPass;\n\tfloat u_Scale;\n\tvec3 u_Tint;\n};\n";
		Assert.assertTrue(StencilShaderTranslatorTest.translate(StencilShaderTranslator.create()).contains(block));
		Assert.assertTrue(StencilShaderTranslatorTest.translate(StencilShaderTranslator.write()).contains(block));
	}

	@Test
	public void declaresTheStencilSamplerAfterTheShaderSamplers() {
		final String translated = StencilShaderTranslatorTest.translate(StencilShaderTranslator.create());
		Assert.assertTrue(translated.indexOf("uniform sampler2D tex;\n") < translated.indexOf("uniform sampler2D joid_Stencil;\n"));
		Assert.assertEquals(Collections.singletonList("tex"), StencilShaderTranslator.create().getSamplers(StencilShaderTranslatorTest.vertex(), StencilShaderTranslatorTest.fragment()).stream().map(ShaderVariable::getName).collect(Collectors.toList()));
	}

	@Test
	public void testsTheStencilOnlyInTheColorVariant() {
		Assert.assertTrue(StencilShaderTranslatorTest.translate(StencilShaderTranslator.create()).contains("if (joid_StencilTest != 0 && !joid_stencilCompare(joid_stencilValue())) {"));
		Assert.assertFalse(StencilShaderTranslatorTest.translate(StencilShaderTranslator.create()).contains("joid_stencilApply(joid_stencilCompare"));
	}

	@Test
	public void writesTheStencilOnlyInTheWriteVariant() {
		Assert.assertTrue(StencilShaderTranslatorTest.translate(StencilShaderTranslator.write()).contains("value = joid_stencilApply(joid_stencilCompare(value) ? joid_StencilPass : joid_StencilFail, value);"));
		Assert.assertFalse(StencilShaderTranslatorTest.translate(StencilShaderTranslator.write()).contains("if (joid_StencilTest != 0"));
	}

	@Test
	public void wrapsTheFragmentMainOnce() {
		for (final CoreShader shader : CoreShader.values()) {
			for (final StencilShaderTranslator translator : new StencilShaderTranslator[] {StencilShaderTranslator.create(), StencilShaderTranslator.write()}) {
				final String translated = translator.translateFragment(shader.read(ShaderStage.VERTEX), shader.read(ShaderStage.FRAGMENT));
				Assert.assertEquals(shader.name(), 1, StencilShaderTranslatorTest.count(translated, "void\\s+joid_main\\s*\\(\\s*\\)"));
				Assert.assertEquals(shader.name(), 1, StencilShaderTranslatorTest.count(translated, "void\\s+main\\s*\\(\\s*\\)"));
				Assert.assertTrue(shader.name(), translated.contains("\tjoid_main();\n"));
			}
		}
	}

	private static String translate(final StencilShaderTranslator translator) {
		return translator.translateFragment(StencilShaderTranslatorTest.vertex(), StencilShaderTranslatorTest.fragment());
	}

	private static ShaderSource vertex() {
		return ShaderSource.parse(ShaderStage.VERTEX, StencilShaderTranslatorTest.VERTEX);
	}

	private static ShaderSource fragment() {
		return ShaderSource.parse(ShaderStage.FRAGMENT, StencilShaderTranslatorTest.FRAGMENT);
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