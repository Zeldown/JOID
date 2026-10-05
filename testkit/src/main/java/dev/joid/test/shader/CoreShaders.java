package dev.joid.test.shader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoreShaders {

	public static @NonNull List<String> getNames() {
		try {
			final URI uri = CoreShaders.class.getResource("/assets/shaders").toURI();
			if (!"jar".equals(uri.getScheme())) {
				return CoreShaders.list(Paths.get(uri));
			}

			try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
				return CoreShaders.list(fileSystem.getPath("/assets/shaders"));
			}
		} catch (final URISyntaxException e) {
			throw new IllegalStateException(e);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static @NonNull ShaderSource read(final @NonNull String name, final @NonNull ShaderStage stage) {
		final String extension = stage == ShaderStage.VERTEX ? ".vsh" : ".fsh";
		return ShaderSource.read(stage, CoreShaders.class.getResourceAsStream("/assets/shaders/" + name + "/" + name + extension));
	}

	private static List<String> list(final Path directory) throws IOException {
		try (Stream<Path> paths = Files.list(directory)) {
			return paths.filter(Files::isDirectory).map(path -> path.getFileName().toString().replace("/", "")).sorted().collect(Collectors.toList());
		}
	}

}