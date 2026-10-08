package dev.joid.test.shader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.shader.source.GlslDialect;
import dev.joid.lib.bridge.render.shader.source.GlslShaderTranslator;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.bridge.render.shader.source.UniformLayout;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlslExport {

	public static void main(final String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Usage: GlslExport <output directory>");
			System.exit(2);
		}

		GlslExport.export(new File(args[0]));
	}

	public static void export(final @NonNull File directory) throws IOException {
		for (final GlslDialect dialect : GlslDialect.values()) {
			final File folder = new File(directory, dialect.name());
			if (!folder.isDirectory() && !folder.mkdirs()) {
				throw new IOException("Unable to create the folder " + folder.getAbsolutePath());
			}

			final GlslShaderTranslator translator = GlslShaderTranslator.create(dialect, dialect.hasUniformBlocks() ? UniformLayout.BLOCK : UniformLayout.LOOSE);
			for (final CoreShader shader : CoreShader.values()) {
				final ShaderSource vertex = shader.read(ShaderStage.VERTEX);
				final ShaderSource fragment = shader.read(ShaderStage.FRAGMENT);
				Files.write(new File(folder, shader.name().toLowerCase(Locale.ROOT) + ".vert").toPath(), translator.translateVertex(vertex, fragment).getBytes(StandardCharsets.UTF_8));
				Files.write(new File(folder, shader.name().toLowerCase(Locale.ROOT) + ".frag").toPath(), translator.translateFragment(vertex, fragment).getBytes(StandardCharsets.UTF_8));
			}
		}
	}

}