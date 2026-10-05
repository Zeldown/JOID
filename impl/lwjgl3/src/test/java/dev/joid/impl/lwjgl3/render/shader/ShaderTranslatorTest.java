package dev.joid.impl.lwjgl3.render.shader;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.test.shader.CoreShaders;

public class ShaderTranslatorTest {

	private static final String VERTEX   = "out vec2 vTexCoord;\n\nvoid main() {\n    vTexCoord = aTexCoord;\n    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);\n}\n";
	private static final String FRAGMENT = "in vec2 vTexCoord;\nflat in vec4 vLitColor;\nuniform sampler2D tex;\n\nvoid main() {\n    fragColor = texture(tex, vTexCoord) * (uLighting ? vLitColor : vec4(1.0));\n}\n";

	@Test
	public void translatesCoreShaders() {
		for (final String name : CoreShaders.getNames()) {
			Assert.assertTrue(name, ShaderTranslator.translate(CoreShaders.read(name, ShaderStage.VERTEX)).startsWith("#version 330 core\n"));
			Assert.assertTrue(name, ShaderTranslator.translate(CoreShaders.read(name, ShaderStage.FRAGMENT)).startsWith("#version 330 core\n"));
		}
	}

	@Test
	public void keepsBodyAfterLineDirective() {
		final ShaderSource source = ShaderSource.parse(ShaderStage.FRAGMENT, ShaderTranslatorTest.FRAGMENT);
		Assert.assertTrue(ShaderTranslator.translate(source).endsWith("#line 1\n" + source.getBody()));
	}

	@Test
	public void alwaysDeclaresFragmentOutput() {
		final String fragment = ShaderTranslator.translate(ShaderSource.parse(ShaderStage.FRAGMENT, "void main() {\n    discard;\n}\n"));
		Assert.assertTrue(fragment.contains("out vec4 fragColor;\n"));
	}

	@Test
	public void declaresAttributesAndMatrices() {
		final String vertex = ShaderTranslator.translate(ShaderSource.parse(ShaderStage.VERTEX, ShaderTranslatorTest.VERTEX));
		Assert.assertTrue(vertex.startsWith("#version 330 core\n"));
		Assert.assertTrue(vertex.contains("layout(location = 0) in vec3 aPosition;\n"));
		Assert.assertTrue(vertex.contains("layout(location = 1) in vec2 aTexCoord;\n"));
		Assert.assertTrue(vertex.contains("uniform mat4 uProjectionMatrix;\n"));
		Assert.assertTrue(vertex.contains("uniform mat4 uModelViewMatrix;\n"));
		Assert.assertTrue(vertex.contains("out vec2 vTexCoord;\n"));
		Assert.assertFalse(vertex.contains("aColor"));
	}

	@Test
	public void declaresFragmentInputsAndOutput() {
		final String fragment = ShaderTranslator.translate(ShaderSource.parse(ShaderStage.FRAGMENT, ShaderTranslatorTest.FRAGMENT));
		Assert.assertTrue(fragment.contains("out vec4 fragColor;\n"));
		Assert.assertTrue(fragment.contains("uniform bool uLighting;\n"));
		Assert.assertTrue(fragment.contains("uniform sampler2D tex;\n"));
		Assert.assertTrue(fragment.contains("in vec2 vTexCoord;\n"));
		Assert.assertTrue(fragment.contains("flat in vec4 vLitColor;\n"));
	}

}