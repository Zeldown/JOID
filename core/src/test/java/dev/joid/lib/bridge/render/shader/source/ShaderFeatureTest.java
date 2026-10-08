package dev.joid.lib.bridge.render.shader.source;

import java.util.EnumSet;

import org.junit.Assert;
import org.junit.Test;

public class ShaderFeatureTest {

	@Test
	public void findsTheFeaturesBeyondGlsl110() {
		Assert.assertEquals(EnumSet.of(ShaderFeature.UNSIGNED_INTEGERS), ShaderFeature.find("uvec2 size = uvec2(1u);"));
		Assert.assertEquals(EnumSet.of(ShaderFeature.BITWISE_OPERATORS), ShaderFeature.find("int bits = value << 2;"));
		Assert.assertEquals(EnumSet.of(ShaderFeature.SWITCH, ShaderFeature.BITWISE_OPERATORS), ShaderFeature.find("switch (mode % 3) {}"));
		Assert.assertEquals(EnumSet.of(ShaderFeature.TEXEL_FETCH), ShaderFeature.find("texelFetch(tex, ivec2(0), 0)"));
		Assert.assertEquals(EnumSet.of(ShaderFeature.TEXTURE_SIZE), ShaderFeature.find("vec2 size = vec2(textureSize(tex, 0));"));
		Assert.assertEquals(EnumSet.of(ShaderFeature.DERIVATIVES), ShaderFeature.find("float width = fwidth(distance);"));
		Assert.assertEquals(EnumSet.of(ShaderFeature.FLAT_VARYINGS), ShaderFeature.find("flat vec4 vColor;"));
	}

	@Test
	public void ignoresTheLogicalOperators() {
		Assert.assertTrue(ShaderFeature.find("if (a && b || c ^^ d) { e = 1; }").isEmpty());
		Assert.assertEquals(EnumSet.of(ShaderFeature.BITWISE_OPERATORS), ShaderFeature.find("e &= 1; f |= 2; g ^= 3;"));
	}

	@Test
	public void findsNoFeatureInTheCoreShaders() {
		for (final CoreShader shader : CoreShader.values()) {
			Assert.assertTrue(shader.name(), shader.read(ShaderStage.VERTEX).getFeatures().isEmpty());
			Assert.assertTrue(shader.name(), shader.read(ShaderStage.FRAGMENT).getFeatures().isEmpty());
		}
	}

	@Test
	public void findsTheFeaturesOfTheDeclarations() {
		final ShaderSource source = ShaderSource.parse(ShaderStage.FRAGMENT, "flat in vec4 vColor;\nuniform uint u_Count;\n\nvoid main() {\n    fragColor = vColor;\n}\n");
		Assert.assertEquals(EnumSet.of(ShaderFeature.FLAT_VARYINGS, ShaderFeature.UNSIGNED_INTEGERS), source.getFeatures());
	}

	@Test
	public void ignoresTheFeaturesOfTheComments() {
		Assert.assertTrue(ShaderSource.parse(ShaderStage.FRAGMENT, "// uint and a << b\nvoid main() {\n    fragColor = vec4(1.0); /* switch */\n}\n").getFeatures().isEmpty());
	}

	@Test
	public void tellsWhichDialectsSupportAFeature() {
		Assert.assertFalse(GlslDialect.GLSL_120.supports(ShaderFeature.UNSIGNED_INTEGERS));
		Assert.assertTrue(GlslDialect.GLSL_130.supports(ShaderFeature.UNSIGNED_INTEGERS));
		Assert.assertTrue(GlslDialect.GLSL_110.supports(ShaderFeature.DERIVATIVES));
		Assert.assertFalse(GlslDialect.ESSL_100.supports(ShaderFeature.DERIVATIVES));
		Assert.assertTrue(GlslDialect.ESSL_300.supports(ShaderFeature.TEXEL_FETCH));
	}

	@Test
	public void namesItsDialects() {
		Assert.assertEquals("GLSL 1.10", GlslDialect.GLSL_110.getName());
		Assert.assertEquals("GLSL 4.50", GlslDialect.GLSL_450.getName());
		Assert.assertEquals("ESSL 3.00", GlslDialect.ESSL_300.getName());
	}

}