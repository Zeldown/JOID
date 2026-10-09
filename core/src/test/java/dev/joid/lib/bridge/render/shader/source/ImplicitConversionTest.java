package dev.joid.lib.bridge.render.shader.source;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class ImplicitConversionTest {

	@Test
	public void writesTheIntegersOfAFloatExpressionAsFloats() {
		Assert.assertEquals("float a = x * 2.0 + pow(y, 3.0) - 1.0;", ImplicitConversionTest.convert("float a = x * 2 + pow(y, 3) - 1;"));
		Assert.assertEquals("vec4 c = vec4(1.0, 0.0, 0.5, 1.0);", ImplicitConversionTest.convert("vec4 c = vec4(1, 0, 0.5, 1);"));
		Assert.assertEquals("if (x > 0.0) {", ImplicitConversionTest.convert("if (x > 0) {"));
	}

	@Test
	public void keepsTheIntegersOfAnIntegerExpression() {
		Assert.assertEquals("for (int i = 0; i < 4; i++) {", ImplicitConversionTest.convert("for (int i = 0; i < 4; i++) {"));
		Assert.assertEquals("ivec2 p = ivec2(1, 2);", ImplicitConversionTest.convert("ivec2 p = ivec2(1, 2);"));
		Assert.assertEquals("float w = u_Weights[2] * 2.0;", ImplicitConversionTest.convert("float w = u_Weights[2] * 2;"));
		Assert.assertEquals("if (u_Type == 2) {", ImplicitConversionTest.convert("if (u_Type == 2) {"));
		Assert.assertEquals("if (2 == u_Type) {", ImplicitConversionTest.convert("if (2 == u_Type) {"));
	}

	@Test
	public void keepsTheArgumentsOfAnIntegerFunction() {
		Assert.assertEquals("int pick(int i) {\n\treturn i;\n}\nfloat a = float(pick(3)) * 2.0;", ImplicitConversionTest.convert("int pick(int i) {\n\treturn i;\n}\nfloat a = float(pick(3)) * 2;"));
	}

	@Test
	public void writesANegativeIntegerAsAFloat() {
		Assert.assertEquals("float a = x * -1.0;", ImplicitConversionTest.convert("float a = x * -1;"));
		Assert.assertEquals("int a = u_Type - 1;", ImplicitConversionTest.convert("int a = u_Type - 1;"));
	}

	@Test
	public void leavesTheOtherLiteralsAndTheDirectivesAlone() {
		Assert.assertEquals("float a = 1.5 + 2e3 + .5 + 1.0f;\n#define COUNT 4\nvec2 v2 = vec2(0.0);", ImplicitConversionTest.convert("float a = 1.5 + 2e3 + .5 + 1.0f;\n#define COUNT 4\nvec2 v2 = vec2(0);"));
	}

	@Test
	public void onlyAddsFractionsToTheCoreShadersForGlsl110() {
		for (final CoreShader shader : CoreShader.values()) {
			final ShaderSource vertex = shader.read(ShaderStage.VERTEX);
			final ShaderSource fragment = shader.read(ShaderStage.FRAGMENT);
			final GlslShaderTranslator old = GlslShaderTranslator.create(GlslDialect.GLSL_110, UniformLayout.LOOSE);
			final GlslShaderTranslator baseline = GlslShaderTranslator.create(GlslDialect.GLSL_120, UniformLayout.LOOSE);
			Assert.assertEquals(shader.name(), ImplicitConversionTest.strip(baseline.translateVertex(vertex, fragment)), ImplicitConversionTest.strip(old.translateVertex(vertex, fragment)));
			Assert.assertEquals(shader.name(), ImplicitConversionTest.strip(baseline.translateFragment(vertex, fragment)), ImplicitConversionTest.strip(old.translateFragment(vertex, fragment)));
		}
	}

	private static String strip(final String translated) {
		return translated.replaceFirst("#version 1[12]0", "#version").replaceAll("(?<![\\w.])(\\d+)\\.0(?![\\d])", "$1");
	}

	private static String convert(final String body) {
		return ImplicitConversion.apply(body, Arrays.asList(ShaderVariable.create("int", "u_Type", "", false), ShaderVariable.create("float", "u_Weights", "[3]", false), ShaderVariable.create("float", "x", "", false)));
	}

}