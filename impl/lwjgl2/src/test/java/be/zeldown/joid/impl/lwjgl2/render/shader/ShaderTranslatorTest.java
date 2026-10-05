package be.zeldown.joid.impl.lwjgl2.render.shader;

import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.lib.bridge.render.shader.source.ShaderSource;
import be.zeldown.joid.lib.bridge.render.shader.source.ShaderStage;
import be.zeldown.joid.test.shader.CoreShaders;

public class ShaderTranslatorTest {

	private static final String VERTEX   = "out vec2 vTexCoord;\n\nvoid main() {\n    vTexCoord = aTexCoord;\n    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);\n}\n";
	private static final String FRAGMENT = "in vec2 vTexCoord;\nflat in vec4 vLitColor;\nuniform sampler2D tex;\nuniform float u_Radius;\n\nvoid main() {\n    fragColor = texture(tex, vTexCoord) * (uLighting ? vLitColor : vec4(u_Radius));\n}\n";

	@Test
	public void translatesCoreShaders() {
		for (final String name : CoreShaders.getNames()) {
			Assert.assertTrue(name, ShaderTranslator.translate(CoreShaders.read(name, ShaderStage.VERTEX)).startsWith("#version 120\n"));
			Assert.assertTrue(name, ShaderTranslator.translate(CoreShaders.read(name, ShaderStage.FRAGMENT)).startsWith("#version 120\n"));
		}
	}

	@Test
	public void declaresFragmentInputs() {
		final String fragment = ShaderTranslator.translate(ShaderSource.parse(ShaderStage.FRAGMENT, ShaderTranslatorTest.FRAGMENT));
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
		final String vertex = ShaderTranslator.translate(ShaderSource.parse(ShaderStage.VERTEX, ShaderTranslatorTest.VERTEX));
		Assert.assertTrue(vertex.startsWith("#version 120\n"));
		Assert.assertTrue(vertex.contains("#define aPosition gl_Vertex.xyz\n"));
		Assert.assertTrue(vertex.contains("#define aTexCoord gl_MultiTexCoord0.xy\n"));
		Assert.assertTrue(vertex.contains("#define uProjectionMatrix gl_ProjectionMatrix\n"));
		Assert.assertTrue(vertex.contains("#define uModelViewMatrix gl_ModelViewMatrix\n"));
		Assert.assertTrue(vertex.contains("varying vec2 vTexCoord;\n"));
		Assert.assertFalse(vertex.contains("aNormal"));
	}

	@Test
	public void keepsBodyAfterLineDirective() {
		final ShaderSource source = ShaderSource.parse(ShaderStage.FRAGMENT, ShaderTranslatorTest.FRAGMENT);
		Assert.assertTrue(ShaderTranslator.translate(source).endsWith("#line 0\n" + source.getBody()));
	}

}