package dev.joid.test.shader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.shader.source.GlslDialect;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;

public class GlslExportTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void writesEveryCoreShaderInEveryDialect() throws IOException {
		GlslExport.export(this.folder.getRoot());
		for (final GlslDialect dialect : GlslDialect.values()) {
			for (final CoreShader shader : CoreShader.values()) {
				for (final String extension : new String[] {".vert", ".frag"}) {
					final File file = new File(new File(this.folder.getRoot(), dialect.name()), shader.name().toLowerCase(Locale.ROOT) + extension);
					Assert.assertTrue(file.getPath(), new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).startsWith(dialect.getDeclaration() + "\n"));
				}
			}
		}
	}

	@Test
	public void compilesTheExportedShadersOfTheModernDialects() throws IOException {
		GlslExport.export(this.folder.getRoot());
		for (final GlslDialect dialect : new GlslDialect[] {GlslDialect.GLSL_330, GlslDialect.GLSL_450}) {
			for (final CoreShader shader : CoreShader.values()) {
				final File output = new File(this.folder.getRoot(), dialect.name());
				Assert.assertTrue(GlslCompiler.compileOpenGl(new String(Files.readAllBytes(new File(output, shader.name().toLowerCase(Locale.ROOT) + ".vert").toPath()), StandardCharsets.UTF_8), ShaderStage.VERTEX).remaining() > 0);
				Assert.assertTrue(GlslCompiler.compileOpenGl(new String(Files.readAllBytes(new File(output, shader.name().toLowerCase(Locale.ROOT) + ".frag").toPath()), StandardCharsets.UTF_8), ShaderStage.FRAGMENT).remaining() > 0);
			}
		}
	}

}