package dev.joid.backend.lwjgl2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;

import dev.joid.lib.cache.FileCache;
import dev.joid.lib.cache.FileCacheArea;
import dev.joid.lib.utils.platform.Platform;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl2Natives {

	private static final FileCacheArea AREA = FileCache.area("lwjgl").retention(Duration.ofDays(30L));

	private static boolean installed;

	public static synchronized void install() {
		if (Lwjgl2Natives.installed) {
			return;
		}

		Lwjgl2Natives.installed = true;
		final String platform = Lwjgl2Natives.getPlatform();
		final String[] libraries = Lwjgl2Natives.getLibraries(platform);
		if (System.getProperty("org.lwjgl.librarypath") != null || Lwjgl2Natives.isProvided(libraries)) {
			return;
		}

		final File directory;
		try {
			directory = Lwjgl2Natives.AREA.compute("2.9.1-" + platform, folder -> {
				Files.createDirectories(folder.toPath());
				for (final String library : libraries) {
					try (InputStream stream = Lwjgl2Natives.class.getResourceAsStream("/dev/joid/backend/lwjgl2/natives/" + platform + "/" + library)) {
						if (stream == null) {
							throw new IllegalStateException("The LWJGL 2 native " + library + " is missing from the backend");
						}

						Files.copy(stream, new File(folder, library).toPath());
					}
				}
			});
		} catch (final IOException exception) {
			throw new IllegalStateException("Unable to extract the LWJGL 2 natives into " + Lwjgl2Natives.AREA.getDirectory().getAbsolutePath() + ", check that the cache folder can be written", exception);
		}
		System.setProperty("org.lwjgl.librarypath", directory.getAbsolutePath());
	}

	private static String getPlatform() {
		switch (Platform.current()) {
		case WINDOWS:
			return "windows";
		case MACOS:
			return "osx";
		default:
			return "linux";
		}
	}

	private static String[] getLibraries(final String platform) {
		switch (platform) {
		case "windows":
			return new String[] {"lwjgl.dll", "lwjgl64.dll", "OpenAL32.dll", "OpenAL64.dll"};
		case "osx":
			return new String[] {"liblwjgl.jnilib", "openal.dylib"};
		default:
			return new String[] {"liblwjgl.so", "liblwjgl64.so", "libopenal.so", "libopenal64.so"};
		}
	}

	private static boolean isProvided(final String[] libraries) {
		final String[] paths = System.getProperty("java.library.path", "").split(File.pathSeparator);
		return Arrays.stream(libraries).allMatch(library -> Arrays.stream(paths).anyMatch(path -> !path.isEmpty() && new File(path, library).isFile()));
	}

}