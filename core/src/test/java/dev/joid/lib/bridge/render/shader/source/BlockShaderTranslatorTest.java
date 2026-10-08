package dev.joid.lib.bridge.render.shader.source;

import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class BlockShaderTranslatorTest {

	private static final String VERTEX   = "out vec2 vTexCoord;\nflat out vec4 vColor;\nuniform float u_Scale;\nuniform vec4 u_Colors[4];\n\nvoid main() {\n    vTexCoord = aTexCoord * u_Scale;\n    vColor = aColor * u_Colors[1];\n    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);\n}\n";
	private static final String FRAGMENT = "in vec2 vTexCoord;\nflat in vec4 vColor;\nuniform sampler2D mask;\nuniform sampler2D tex;\nuniform vec3 u_Tint;\nuniform float u_Scale;\n\nvoid main() {\n    fragColor = texture(tex, vTexCoord) * texture(mask, vTexCoord) * vColor * vec4(u_Tint, uLighting ? 1.0 : 0.5);\n}\n";

	private static final String BLOCK = "layout(std140) uniform JoidUniforms {\n\tmat4 uProjectionMatrix;\n\tmat4 uModelViewMatrix;\n\tbool uLighting;\n\tint joid_AlphaTest;\n\tfloat joid_AlphaThreshold;\n\tfloat u_Scale;\n\tvec4 u_Colors[4];\n\tvec3 u_Tint;\n};\n";

	@Test
	public void sharesTheUniformBlockBetweenStages() {
		Assert.assertTrue(BlockShaderTranslatorTest.translateVertex().startsWith("#version 330 core\n\n" + BlockShaderTranslatorTest.BLOCK));
		Assert.assertTrue(BlockShaderTranslatorTest.translateFragment().startsWith("#version 330 core\n\n" + BlockShaderTranslatorTest.BLOCK));
	}

	@Test
	public void declaresTheAttributesAtTheirLocations() {
		final String vertex = BlockShaderTranslatorTest.translateVertex();
		Assert.assertTrue(vertex.contains("layout(location = 0) in vec3 aPosition;\n"));
		Assert.assertTrue(vertex.contains("layout(location = 1) in vec2 aTexCoord;\n"));
		Assert.assertTrue(vertex.contains("layout(location = 2) in vec4 aColor;\n"));
		Assert.assertFalse(vertex.contains("aNormal"));
	}

	@Test
	public void declaresTheVaryingsWithTheirInterpolation() {
		Assert.assertTrue(BlockShaderTranslatorTest.translateVertex().contains("out vec2 vTexCoord;\nflat out vec4 vColor;\n"));
		Assert.assertTrue(BlockShaderTranslatorTest.translateFragment().contains("in vec2 vTexCoord;\nflat in vec4 vColor;\n"));
	}

	@Test
	public void declaresTheSamplersOfTheirStageOnly() {
		Assert.assertFalse(BlockShaderTranslatorTest.translateVertex().contains("sampler2D"));
		Assert.assertTrue(BlockShaderTranslatorTest.translateFragment().contains("layout(location = 0) out vec4 fragColor;\nuniform sampler2D mask;\nuniform sampler2D tex;\n"));
	}

	@Test
	public void wrapsTheFragmentMainWithTheAlphaTest() {
		final String fragment = BlockShaderTranslatorTest.translateFragment();
		Assert.assertTrue(fragment.contains("\nvoid joid_main() {\n"));
		Assert.assertTrue(fragment.endsWith("\nvoid main() {\n\tjoid_main();\n\tif (joid_AlphaTest != 0 && fragColor.a <= joid_AlphaThreshold) {\n\t\tdiscard;\n\t}\n}\n"));
	}

	@Test
	public void keepsTheLineNumbersOfTheBody() {
		final String fragment = BlockShaderTranslatorTest.translateFragment();
		final String[] lines = fragment.substring(fragment.indexOf("#line 1\n") + "#line 1\n".length()).split("\n", -1);
		Assert.assertEquals("    fragColor = texture(tex, vTexCoord) * texture(mask, vTexCoord) * vColor * vec4(u_Tint, uLighting ? 1.0 : 0.5);", lines[8]);
	}

	@Test
	public void listsTheUniformsOnceForBothStages() {
		final ShaderSource vertex = ShaderSource.parse(ShaderStage.VERTEX, BlockShaderTranslatorTest.VERTEX);
		final ShaderSource fragment = ShaderSource.parse(ShaderStage.FRAGMENT, BlockShaderTranslatorTest.FRAGMENT);
		Assert.assertEquals("uProjectionMatrix uModelViewMatrix uLighting joid_AlphaTest joid_AlphaThreshold u_Scale u_Colors u_Tint", BlockShaderTranslator.create().getUniforms(vertex, fragment).stream().map(ShaderVariable::getName).collect(Collectors.joining(" ")));
		Assert.assertEquals("mask tex", BlockShaderTranslator.create().getSamplers(vertex, fragment).stream().map(ShaderVariable::getName).collect(Collectors.joining(" ")));
	}

	@Test
	public void createsTheBlockOfTheTranslatedUniforms() {
		final ShaderSource vertex = ShaderSource.parse(ShaderStage.VERTEX, BlockShaderTranslatorTest.VERTEX);
		final ShaderSource fragment = ShaderSource.parse(ShaderStage.FRAGMENT, BlockShaderTranslatorTest.FRAGMENT);
		Assert.assertEquals(8, BlockShaderTranslator.create().createBlock(vertex, fragment).getMemberMap().size());
		Assert.assertEquals(4, BlockShaderTranslator.create().createBlock(vertex, fragment).getMember("u_Colors").getLength());
	}

	private static String translateVertex() {
		return BlockShaderTranslator.create().translateVertex(ShaderSource.parse(ShaderStage.VERTEX, BlockShaderTranslatorTest.VERTEX), ShaderSource.parse(ShaderStage.FRAGMENT, BlockShaderTranslatorTest.FRAGMENT));
	}

	private static String translateFragment() {
		return BlockShaderTranslator.create().translateFragment(ShaderSource.parse(ShaderStage.VERTEX, BlockShaderTranslatorTest.VERTEX), ShaderSource.parse(ShaderStage.FRAGMENT, BlockShaderTranslatorTest.FRAGMENT));
	}

}