package dev.joid.impl.vulkan.render.shader;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.test.shader.CoreShaders;

public class ShaderTranslatorTest {

	private static final String VERTEX   = "out vec2 vPosition;\nout vec2 vTexCoord;\nout vec4 vColor;\nuniform float u_Scale;\n\nvoid main() {\n    vPosition = aPosition.xy * u_Scale;\n    vTexCoord = aTexCoord;\n    vColor = aColor;\n    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);\n}\n";
	private static final String FRAGMENT = "in vec4 vColor;\nin vec2 vTexCoord;\nuniform sampler2D mask;\nuniform sampler2D tex;\nuniform vec4 u_Color;\n\nvoid main() {\n    fragColor = texture(tex, vTexCoord) * texture(mask, vTexCoord) * vColor * (uLighting ? u_Color : vec4(1.0));\n}\n";

	private static final String BLOCK = "layout(std140, binding = 0) uniform JoidUniforms {\n\tmat4 uProjectionMatrix;\n\tmat4 uModelViewMatrix;\n\tbool uLighting;\n\tvec4 joid_CurrentColor;\n\tint joid_VertexColor;\n\tfloat u_Scale;\n\tvec4 u_Color;\n};\n";

	@Test
	public void compilesCoreShaders() {
		for (final String name : CoreShaders.getNames()) {
			final ShaderSource vertex = CoreShaders.read(name, ShaderStage.VERTEX);
			final ShaderSource fragment = CoreShaders.read(name, ShaderStage.FRAGMENT);
			Assert.assertTrue(name, ShaderCompiler.compileVertex(ShaderTranslator.translateVertex(vertex, fragment)).remaining() > 0);
			Assert.assertTrue(name, ShaderCompiler.compileFragment(ShaderTranslator.translateFragment(vertex, fragment)).remaining() > 0);
		}
	}

	@Test
	public void alignsVaryingLocations() {
		final ShaderSource vertex = ShaderSource.parse(ShaderStage.VERTEX, ShaderTranslatorTest.VERTEX);
		final String fragment = ShaderTranslator.translateFragment(vertex, ShaderSource.parse(ShaderStage.FRAGMENT, ShaderTranslatorTest.FRAGMENT));
		Assert.assertTrue(fragment.contains("layout(location = 2) in vec4 vColor;\n"));
		Assert.assertTrue(fragment.contains("layout(location = 1) in vec2 vTexCoord;\n"));
		Assert.assertTrue(fragment.contains("layout(location = 0) out vec4 fragColor;\n"));
	}

	@Test
	public void redirectsColorToCurrentColor() {
		final String vertex = ShaderTranslator.translateVertex(ShaderSource.parse(ShaderStage.VERTEX, ShaderTranslatorTest.VERTEX), ShaderSource.parse(ShaderStage.FRAGMENT, ShaderTranslatorTest.FRAGMENT));
		Assert.assertTrue(vertex.contains("layout(location = 2) in vec4 joid_Color;\n"));
		Assert.assertTrue(vertex.contains("#define aColor (joid_VertexColor != 0 ? joid_Color : joid_CurrentColor)\n"));
	}

	@Test
	public void bindsSamplersAfterUniformBlock() {
		final ShaderSource vertex = ShaderSource.parse(ShaderStage.VERTEX, ShaderTranslatorTest.VERTEX);
		final String fragment = ShaderTranslator.translateFragment(vertex, ShaderSource.parse(ShaderStage.FRAGMENT, ShaderTranslatorTest.FRAGMENT));
		Assert.assertTrue(fragment.contains("layout(binding = 1) uniform sampler2D mask;\n"));
		Assert.assertTrue(fragment.contains("layout(binding = 2) uniform sampler2D tex;\n"));
	}

	@Test
	public void sharesUniformBlockBetweenStages() {
		final ShaderSource vertex = ShaderSource.parse(ShaderStage.VERTEX, ShaderTranslatorTest.VERTEX);
		final ShaderSource fragment = ShaderSource.parse(ShaderStage.FRAGMENT, ShaderTranslatorTest.FRAGMENT);
		Assert.assertTrue(ShaderTranslator.translateVertex(vertex, fragment).contains(ShaderTranslatorTest.BLOCK));
		Assert.assertTrue(ShaderTranslator.translateFragment(vertex, fragment).contains(ShaderTranslatorTest.BLOCK));
	}

}