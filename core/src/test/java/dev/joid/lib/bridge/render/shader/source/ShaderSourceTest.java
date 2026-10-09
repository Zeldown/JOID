package dev.joid.lib.bridge.render.shader.source;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.EnumSet;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ShaderSourceTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void readsAnyAssetHandle() throws IOException {
		final File file = this.folder.newFile("wave.fsh");
		Files.write(file.toPath(), "uniform float u_Time;\n\nvoid main() {\n}\n".getBytes(StandardCharsets.UTF_8));
		Assert.assertEquals("u_Time", ShaderSource.read(ShaderStage.FRAGMENT, file).getUniforms().get(0).getName());
		Assert.assertEquals("u_Time", ShaderSource.read(ShaderStage.FRAGMENT, file.toURI().toString()).getUniforms().get(0).getName());
	}

	@Test
	public void closesTheStreamItReads() {
		final boolean[] closed = new boolean[1];
		ShaderSource.read(ShaderStage.FRAGMENT, new ByteArrayInputStream("void main() {}".getBytes(StandardCharsets.UTF_8)) {

			@Override
			public void close() throws IOException {
				closed[0] = true;
				super.close();
			}

		});
		Assert.assertTrue(closed[0]);
	}

	@Test
	public void parsesVaryings() {
		final ShaderSource source = ShaderSource.parse(ShaderStage.VERTEX, "out vec2 vTexCoord;\nflat out vec4 vColor;\n\nvoid main() {\n}\n");
		Assert.assertEquals(2, source.getOutputs().size());
		Assert.assertEquals("vec2 vTexCoord", source.getOutputs().get(0).getDeclaration());
		Assert.assertFalse(source.getOutputs().get(0).isFlat());
		Assert.assertTrue(source.getOutputs().get(1).isFlat());
	}

	@Test
	public void keepsLineNumbers() {
		final String[] lines = ShaderSource.parse(ShaderStage.FRAGMENT, "uniform float u_Radius;\nin vec2 vTexCoord;\n\nvoid main() {\n    fragColor = vec4(u_Radius);\n}").getBody().split("\n", -1);
		Assert.assertEquals("", lines[0]);
		Assert.assertEquals("", lines[1]);
		Assert.assertEquals("    fragColor = vec4(u_Radius);", lines[4]);
	}

	@Test
	public void detectsUsedBuiltins() {
		final ShaderSource source = ShaderSource.parse(ShaderStage.VERTEX, "// uNormalMatrix is unused\nvoid main() {\n    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0); /* aNormal */\n}\n");
		Assert.assertEquals(EnumSet.of(ShaderBuiltin.POSITION, ShaderBuiltin.PROJECTION_MATRIX, ShaderBuiltin.MODEL_VIEW_MATRIX), source.getBuiltins());
	}

	@Test
	public void turnsAVertexShaderIntoALineShader() {
		final ShaderSource source = ShaderSource.parse(ShaderStage.VERTEX, "out vec4 vColor;\nuniform float u_Wave;\n\nvoid main() {\n    vColor = aColor;\n    gl_Position = vec4(aPosition, u_Wave);\n}\n");
		final ShaderSource line = source.toLine();
		Assert.assertFalse(source.isLine());
		Assert.assertTrue(line.isLine());
		Assert.assertEquals(EnumSet.of(ShaderBuiltin.POSITION, ShaderBuiltin.COLOR), source.getBuiltins());
		Assert.assertEquals(EnumSet.of(ShaderBuiltin.POSITION, ShaderBuiltin.TEXTURE_COORDINATE, ShaderBuiltin.COLOR, ShaderBuiltin.NORMAL), line.getBuiltins());
		Assert.assertEquals(source.getBody(), line.getBody());
		Assert.assertEquals(source.getOutputs(), line.getOutputs());
		Assert.assertEquals(source.getUniforms(), line.getUniforms());
	}

	@Test(expected = IllegalStateException.class)
	public void refusesALineFragmentShader() {
		ShaderSource.parse(ShaderStage.FRAGMENT, "void main() {\n    fragColor = vec4(1.0);\n}\n").toLine();
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsVertexInputs() {
		ShaderSource.parse(ShaderStage.VERTEX, "in vec3 aCustom;\n");
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsFragmentOutputs() {
		ShaderSource.parse(ShaderStage.FRAGMENT, "out vec4 color;\n");
	}

	@Test
	public void separatesUniformsAndSamplers() {
		final ShaderSource source = ShaderSource.parse(ShaderStage.FRAGMENT, "uniform sampler2D tex;\nuniform vec4 u_Color;\nuniform float u_Values[16];\n");
		Assert.assertEquals(1, source.getSamplers().size());
		Assert.assertEquals("tex", source.getSamplers().get(0).getName());
		Assert.assertEquals(2, source.getUniforms().size());
		Assert.assertEquals("float u_Values[16]", source.getUniforms().get(1).getDeclaration());
	}

	@Test
	public void ignoresVersionAndBuiltinDeclarations() {
		final ShaderSource source = ShaderSource.parse(ShaderStage.FRAGMENT, "#version 330 core\nlayout(location = 0) out vec4 fragColor;\nuniform bool uLighting;\n\nvoid main() {\n    fragColor = vec4(uLighting ? 1.0 : 0.0);\n}\n");
		Assert.assertTrue(source.getOutputs().isEmpty());
		Assert.assertTrue(source.getUniforms().isEmpty());
		Assert.assertEquals(EnumSet.of(ShaderBuiltin.LIGHTING, ShaderBuiltin.FRAGMENT_COLOR), source.getBuiltins());
		Assert.assertFalse(source.getBody().contains("#version"));
	}

	@Test
	public void findsABuiltinByItsIdentifier() {
		Assert.assertSame(ShaderBuiltin.POSITION, ShaderBuiltin.find("aPosition"));
		Assert.assertNull(ShaderBuiltin.find("aMissing"));
	}

}