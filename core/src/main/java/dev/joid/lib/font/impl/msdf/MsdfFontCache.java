package dev.joid.lib.font.impl.msdf;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

import com.google.common.hash.Hashing;

import dev.joid.internal.JOID;
import dev.joid.msdf.MsdfGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MsdfFontCache {

	private static final String SIGNATURE = JOID.VERSION + " " + MsdfGenerator.CHARSET + " " + MsdfGenerator.RANGE + " " + MsdfGenerator.WIDTH + "x" + MsdfGenerator.HEIGHT;

	@Getter
	private static File directory = MsdfFontCache.locateDirectory();

	public static void directory(final @NonNull File directory) {
		MsdfFontCache.directory = directory;
	}

	public static @NonNull File resolve(final @NonNull byte[] font) throws IOException {
		final File file = new File(MsdfFontCache.directory, Hashing.sha256().newHasher().putBytes(MsdfFontCache.SIGNATURE.getBytes(StandardCharsets.UTF_8)).putBytes(font).hash() + ".msdf");
		if (file.isFile()) {
			return file;
		}

		if (!MsdfFontCache.directory.isDirectory() && !MsdfFontCache.directory.mkdirs()) {
			throw new IOException("Unable to create the msdf cache " + MsdfFontCache.directory.getAbsolutePath());
		}

		final File temporary = File.createTempFile("msdf-", ".tmp", MsdfFontCache.directory);
		try {
			MsdfGenerator.generate(font, temporary, MsdfGenerator.codepoints(MsdfGenerator.CHARSET), MsdfGenerator.WIDTH, MsdfGenerator.HEIGHT, MsdfGenerator.RANGE, 0D);
			Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (final Exception exception) {
			throw new IOException("Unable to generate the msdf atlas of the font in " + MsdfFontCache.directory.getAbsolutePath(), exception);
		} finally {
			Files.deleteIfExists(temporary.toPath());
		}
		return file;
	}

	private static @NonNull File locateDirectory() {
		final String system = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
		final String home = System.getProperty("user.home");
		if (system.contains("win")) {
			final String local = System.getenv("LOCALAPPDATA");
			return new File(local != null ? new File(local) : new File(home, "AppData/Local"), "joid/msdf");
		}

		if (system.contains("mac")) {
			return new File(home, "Library/Caches/joid/msdf");
		}

		final String cache = System.getenv("XDG_CACHE_HOME");
		return new File(cache != null ? new File(cache) : new File(home, ".cache"), "joid/msdf");
	}

}