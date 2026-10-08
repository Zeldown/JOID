package dev.joid.backend.lwjgl2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;

import dev.joid.lib.utils.platform.Platform;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Natives {

	private static boolean installed;

	public static synchronized void install() {
		if (Natives.installed) {
			return;
		}

		Natives.installed = true;
		final String platform = Natives.getPlatform();
		final String[] libraries = Natives.getLibraries(platform);
		if (System.getProperty("org.lwjgl.librarypath") != null || Natives.isProvided(libraries)) {
			return;
		}

		final File directory = new File(new File(System.getProperty("java.io.tmpdir"), "joid-lwjgl-2.9.1"), platform);
		if (!directory.isDirectory() && !directory.mkdirs()) {
			throw new IllegalStateException("Unable to create the LWJGL 2 natives folder " + directory.getAbsolutePath());
		}

		for (final String library : libraries) {
			final File target = new File(directory, library);
			try (InputStream stream = Natives.class.getResourceAsStream("/dev/joid/backend/lwjgl2/natives/" + platform + "/" + library)) {
				if (stream == null) {
					throw new IllegalStateException("The LWJGL 2 native " + library + " is missing from the backend");
				}

				final byte[] bytes = IOUtils.toByteArray(stream);
				if (!target.isFile() || target.length() != bytes.length) {
					Files.write(target.toPath(), bytes);
				}
			} catch (final IOException exception) {
				throw new IllegalStateException("Unable to extract the LWJGL 2 native " + library, exception);
			}
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
		for (final String path : System.getProperty("java.library.path", "").split(File.pathSeparator)) {
			for (final String library : libraries) {
				if (!path.isEmpty() && new File(path, library).isFile()) {
					return true;
				}
			}
		}
		return false;
	}

}