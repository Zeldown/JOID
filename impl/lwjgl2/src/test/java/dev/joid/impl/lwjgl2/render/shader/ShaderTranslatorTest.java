package dev.joid.impl.lwjgl2.render.shader;

import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.bridge.render.shader.source.ShaderVariable;

public class ShaderTranslatorTest {

	private static final String VERTEX   = "out vec2 vTexCoord;\n\nvoid main() {\n    vTexCoord = aTexCoord;\n    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);\n}\n";
	private static final String FRAGMENT = "in vec2 vTexCoord;\nflat in vec4 vLitColor;\nuniform sampler2D tex;\nuniform float u_Radius;\n\nvoid main() {\n    fragColor = texture(tex, vTexCoord) * (uLighting ? vLitColor : vec4(u_Radius));\n}\n";

	@Test
	public void translatesCoreShaders() {
		for (final CoreShader shader : CoreShader.values()) {
			Assert.assertTrue(shader.name(), ShaderTranslatorTest.translate(shader.read(ShaderStage.VERTEX)).startsWith("#version 120\n"));
			Assert.assertTrue(shader.name(), ShaderTranslatorTest.translate(shader.read(ShaderStage.FRAGMENT)).startsWith("#version 120\n"));
		}
	}

	@Test
	public void declaresFragmentInputs() {
		final String fragment = ShaderTranslatorTest.translate(ShaderSource.parse(ShaderStage.FRAGMENT, ShaderTranslatorTest.FRAGMENT));
		Assert.assertTrue(fragment.contains("#define texture texture2D\n"));
		Assert.assertTrue(fragment.contains("#define fragColor gl_FragColor\n"));
		Assert.assertTrue(fragment.contains("uniform bool uLighting;\n"));
		Assert.assertTrue(fragment.contains("uniform sampler2D tex;\n"));
		Assert.assertTrue(fragment.contains("uniform float u_Radius;\n"));
		Assert.assertTrue(fragment.contains("varying vec4 vLitColor;\n"));
		Assert.assertFalse(fragment.contains("flat"));
	}

	@Test
	public void mapsBuiltinsToFixedPipeline() {
		final String vertex = ShaderTranslatorTest.translate(ShaderSource.parse(ShaderStage.VERTEX, ShaderTranslatorTest.VERTEX));
		Assert.assertTrue(vertex.startsWith("#version 120\n"));
		Assert.assertTrue(vertex.contains("#define aPosition gl_Vertex.xyz\n"));
		Assert.assertTrue(vertex.contains("#define aTexCoord gl_MultiTexCoord0.xy\n"));
		Assert.assertTrue(vertex.contains("#define uProjectionMatrix gl_ProjectionMatrix\n"));
		Assert.assertTrue(vertex.contains("#define uModelViewMatrix gl_ModelViewMatrix\n"));
		Assert.assertTrue(vertex.contains("varying vec2 vTexCoord;\n"));
		Assert.assertFalse(vertex.contains("aNormal"));
	}

	@Test
	public void readsTheExactByteNormalsLikeTheOtherBackends() {
		final String vertex = ShaderTranslatorTest.translate(CoreShader.FIXED.read(ShaderStage.VERTEX));
		Assert.assertTrue(vertex.contains("attribute vec3 joid_Normal;\n#define aNormal (joid_Normal / 127.0)\n"));
		Assert.assertEquals(6, Shader.NORMAL_LOCATION);
	}

	@Test
	public void keepsBodyAfterLineDirective() {
		final ShaderSource source = ShaderSource.parse(ShaderStage.FRAGMENT, ShaderTranslatorTest.FRAGMENT);
		Assert.assertTrue(ShaderTranslatorTest.translate(source).endsWith("#line 0\n" + source.getBody()));
	}

	@Test
	public void declaresOnlyTheLightingAmongTheBuiltinUniforms() {
		final ShaderSource vertex = ShaderSource.parse(ShaderStage.VERTEX, ShaderTranslatorTest.VERTEX);
		final ShaderSource fragment = ShaderSource.parse(ShaderStage.FRAGMENT, ShaderTranslatorTest.FRAGMENT);
		Assert.assertEquals("uLighting u_Radius", ShaderTranslator.create().getUniforms(vertex, fragment).stream().map(ShaderVariable::getName).collect(Collectors.joining(" ")));
	}

	private static String translate(final ShaderSource source) {
		final ShaderSource vertex = ShaderSource.parse(ShaderStage.VERTEX, "");
		final ShaderSource fragment = ShaderSource.parse(ShaderStage.FRAGMENT, "");
		return source.getStage() == ShaderStage.VERTEX ? ShaderTranslator.create().translateVertex(source, fragment) : ShaderTranslator.create().translateFragment(vertex, source);
	}

}