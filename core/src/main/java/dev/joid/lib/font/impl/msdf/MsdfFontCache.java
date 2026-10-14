package dev.joid.lib.font.impl.msdf;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.google.common.hash.Hashing;

import dev.joid.internal.JOID;
import dev.joid.lib.cache.FileCache;
import dev.joid.lib.cache.FileCacheArea;
import dev.joid.tool.msdf.MsdfGenerator;
import dev.joid.tool.msdf.atlas.MsdfWriter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MsdfFontCache {

	private static final FileCacheArea AREA = FileCache.area("msdf").retention(Duration.ofDays(30L));

	public static File find(final @NonNull byte[] font) {
		return MsdfFontCache.AREA.entry(MsdfFontCache.key(font));
	}

	public static @NonNull File resolve(final @NonNull byte[] font) throws IOException {
		return MsdfFontCache.AREA.compute(MsdfFontCache.key(font), file -> MsdfFontCache.generate(font, file));
	}

	public static @NonNull File regenerate(final @NonNull byte[] font) throws IOException {
		MsdfFontCache.AREA.remove(MsdfFontCache.key(font));
		return MsdfFontCache.resolve(font);
	}

	private static void generate(final byte[] font, final File file) throws IOException {
		try {
			MsdfGenerator.generate(font, file, MsdfGenerator.codepoints(MsdfGenerator.CHARSET), MsdfGenerator.WIDTH, MsdfGenerator.HEIGHT, MsdfGenerator.RANGE, 0D);
		} catch (final Exception exception) {
			throw new IOException("The msdf atlas of the font cannot be generated in " + file.getParentFile().getAbsolutePath() + ", check that the font is a valid TrueType or OpenType file and that the cache folder can be written", exception);
		}
	}

	private static @NonNull String key(final byte[] font) {
		return Hashing.sha256().newHasher().putString(JOID.VERSION + " " + MsdfWriter.VERSION + " " + MsdfGenerator.CHARSET + " " + MsdfGenerator.RANGE + " " + MsdfGenerator.WIDTH + "x" + MsdfGenerator.HEIGHT, StandardCharsets.UTF_8).putBytes(font).hash() + ".msdf";
	}

}