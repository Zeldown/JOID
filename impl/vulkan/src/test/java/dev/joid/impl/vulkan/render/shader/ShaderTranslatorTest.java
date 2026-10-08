package dev.joid.impl.vulkan.render.shader;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.shader.source.BlockShaderTranslator;
import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.bridge.render.shader.source.StencilShaderTranslator;
import dev.joid.lib.bridge.render.shader.uniform.UniformBlock;
import dev.joid.lib.bridge.render.shader.uniform.UniformMember;

public class ShaderTranslatorTest {

	private static final String VERTEX   = "out vec2 vPosition;\nout vec2 vTexCoord;\nout vec4 vColor;\nuniform float u_Scale;\nuniform vec4 u_Colors[4];\n\nvoid main() {\n    vPosition = aPosition.xy * u_Scale;\n    vTexCoord = aTexCoord;\n    vColor = aColor * u_Colors[1];\n    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);\n}\n";
	private static final String FRAGMENT = "in vec4 vColor;\nin vec2 vTexCoord;\nuniform sampler2D mask;\nuniform sampler2D tex;\nuniform vec4 u_Color;\nuniform mat3 u_Transform;\n\nvoid main() {\n    fragColor = texture(tex, (u_Transform * vec3(vTexCoord, 1.0)).xy) * texture(mask, vTexCoord) * vColor * (uLighting ? u_Color : vec4(1.0));\n}\n";

	private static final String BLOCK = "layout(std140, binding = 0) uniform JoidUniforms {\n\tmat4 uProjectionMatrix;\n\tmat4 uModelViewMatrix;\n\tbool uLighting;\n\tint joid_AlphaTest;\n\tfloat joid_AlphaThreshold;\n\tvec4 joid_CurrentColor;\n\tint joid_VertexColor;\n\tfloat u_Scale;\n\tvec4 u_Colors[4];\n\tvec4 u_Color;\n\tmat3 u_Transform;\n};\n";

	@Test
	public void compilesCoreShaders() {
		for (final CoreShader shader : CoreShader.values()) {
			final ShaderSource vertex = shader.read(ShaderStage.VERTEX);
			final ShaderSource fragment = shader.read(ShaderStage.FRAGMENT);
			Assert.assertTrue(shader.name(), ShaderCompiler.compileVertex(ShaderTranslator.create().translateVertex(vertex, fragment)).remaining() > 0);
			Assert.assertTrue(shader.name(), ShaderCompiler.compileFragment(ShaderTranslator.create().translateFragment(vertex, fragment)).remaining() > 0);
		}
	}

	@Test
	public void compilesCoreShadersWithTheDefaultBlockTranslator() {
		for (final CoreShader shader : CoreShader.values()) {
			final ShaderSource vertex = shader.read(ShaderStage.VERTEX);
			final ShaderSource fragment = shader.read(ShaderStage.FRAGMENT);
			Assert.assertTrue(shader.name(), GlslCompiler.compileOpenGl(BlockShaderTranslator.create().translateVertex(vertex, fragment), ShaderStage.VERTEX).remaining() > 0);
			Assert.assertTrue(shader.name(), GlslCompiler.compileOpenGl(BlockShaderTranslator.create().translateFragment(vertex, fragment), ShaderStage.FRAGMENT).remaining() > 0);
		}
	}

	@Test
	public void compilesCoreShadersWithTheStencilEmulation() {
		for (final CoreShader shader : CoreShader.values()) {
			final ShaderSource vertex = shader.read(ShaderStage.VERTEX);
			final ShaderSource fragment = shader.read(ShaderStage.FRAGMENT);
			Assert.assertTrue(shader.name(), GlslCompiler.compileOpenGl(StencilShaderTranslator.create().translateFragment(vertex, fragment), ShaderStage.FRAGMENT).remaining() > 0);
			Assert.assertTrue(shader.name(), GlslCompiler.compileOpenGl(StencilShaderTranslator.write().translateFragment(vertex, fragment), ShaderStage.FRAGMENT).remaining() > 0);
		}
	}

	@Test
	public void layoutsTheBlockOfEveryCoreShaderLikeTheCompiler() {
		for (final CoreShader shader : CoreShader.values()) {
			final ShaderSource vertex = shader.read(ShaderStage.VERTEX);
			final ShaderSource fragment = shader.read(ShaderStage.FRAGMENT);
			ShaderTranslatorTest.assertLayout(shader.name(), ShaderTranslator.create().createBlock(vertex, fragment), SpirvBlockLayout.read(ShaderCompiler.compileFragment(ShaderTranslator.create().translateFragment(vertex, fragment)), BlockShaderTranslator.BLOCK));
		}
	}

	@Test
	public void layoutsTheBlockLikeTheCompiler() {
		final ShaderSource vertex = ShaderSource.parse(ShaderStage.VERTEX, ShaderTranslatorTest.VERTEX);
		final ShaderSource fragment = ShaderSource.parse(ShaderStage.FRAGMENT, ShaderTranslatorTest.FRAGMENT);
		ShaderTranslatorTest.assertLayout("test", ShaderTranslator.create().createBlock(vertex, fragment), SpirvBlockLayout.read(ShaderCompiler.compileFragment(ShaderTranslator.create().translateFragment(vertex, fragment)), BlockShaderTranslator.BLOCK));
	}

	@Test
	public void alignsVaryingLocations() {
		final String fragment = ShaderTranslatorTest.translateFragment();
		Assert.assertTrue(fragment.contains("layout(location = 2) in vec4 vColor;\n"));
		Assert.assertTrue(fragment.contains("layout(location = 1) in vec2 vTexCoord;\n"));
		Assert.assertTrue(fragment.contains("layout(location = 0) out vec4 fragColor;\n"));
	}

	@Test
	public void redirectsColorToCurrentColor() {
		final String vertex = ShaderTranslatorTest.translateVertex();
		Assert.assertTrue(vertex.contains("layout(location = 2) in vec4 joid_Color;\n"));
		Assert.assertTrue(vertex.contains("#define aColor (joid_VertexColor != 0 ? joid_Color : joid_CurrentColor)\n"));
	}

	@Test
	public void bindsSamplersAfterUniformBlock() {
		final String fragment = ShaderTranslatorTest.translateFragment();
		Assert.assertTrue(fragment.contains("layout(binding = 1) uniform sampler2D mask;\n"));
		Assert.assertTrue(fragment.contains("layout(binding = 2) uniform sampler2D tex;\n"));
	}

	@Test
	public void sharesUniformBlockBetweenStages() {
		Assert.assertTrue(ShaderTranslatorTest.translateVertex().contains(ShaderTranslatorTest.BLOCK));
		Assert.assertTrue(ShaderTranslatorTest.translateFragment().contains(ShaderTranslatorTest.BLOCK));
	}

	private static String translateVertex() {
		return ShaderTranslator.create().translateVertex(ShaderSource.parse(ShaderStage.VERTEX, ShaderTranslatorTest.VERTEX), ShaderSource.parse(ShaderStage.FRAGMENT, ShaderTranslatorTest.FRAGMENT));
	}

	private static String translateFragment() {
		return ShaderTranslator.create().translateFragment(ShaderSource.parse(ShaderStage.VERTEX, ShaderTranslatorTest.VERTEX), ShaderSource.parse(ShaderStage.FRAGMENT, ShaderTranslatorTest.FRAGMENT));
	}

	private static void assertLayout(final String name, final UniformBlock block, final Map<String, int[]> layoutMap) {
		Assert.assertEquals(name, block.getMemberMap().keySet(), layoutMap.keySet());
		for (final UniformMember member : block.getMemberMap().values()) {
			Assert.assertArrayEquals(name + " " + member.getName(), new int[] {member.getOffset(), member.getArrayStride(), member.getMatrixStride()}, layoutMap.get(member.getName()));
		}
	}

}