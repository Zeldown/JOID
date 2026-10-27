package dev.joid.lib.font.impl.msdf.source;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;

import com.google.common.hash.Hashing;

import dev.joid.lib.JOID;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.cache.FileCacheArea;
import dev.joid.lib.cache.FileCacheWriter;
import dev.joid.lib.font.impl.msdf.MsdfFontFace;
import dev.joid.tool.msdf.MsdfGenerator;
import dev.joid.tool.msdf.atlas.MsdfWriter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenTypeMsdfSource extends MsdfSource {

	public static final int HEADER = 4;

	private static final byte[][] SIGNATURES = {{0, 1, 0, 0}, {'t', 'r', 'u', 'e'}, {'O', 'T', 'T', 'O'}, {'t', 't', 'c', 'f'}};

	private static final FileCacheArea AREA = FileCacheArea.of("msdf").retention(Duration.ofDays(30L));

	private final Asset asset;

	private File    file;
	private boolean generated;

	public static @NonNull OpenTypeMsdfSource of(final @NonNull Object handle) {
		return new OpenTypeMsdfSource(Asset.of(handle));
	}

	public static boolean supports(final @NonNull byte[] header) {
		for (final byte[] signature : OpenTypeMsdfSource.SIGNATURES) {
			if (header.length >= signature.length && Arrays.equals(Arrays.copyOf(header, signature.length), signature)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public @NonNull String describe() {
		return (this.generated ? "generated into the msdf cache (" : "read from the msdf cache (") + this.file + ")";
	}

	@Override
	protected @NonNull MsdfFontFace parse() throws IOException {
		final byte[] font = this.asset.read();
		final String key = OpenTypeMsdfSource.key(font);
		final File cached = OpenTypeMsdfSource.AREA.entry(key);
		this.generated = cached == null;
		this.file = this.generated ? OpenTypeMsdfSource.AREA.compute(key, OpenTypeMsdfSource.generator(font)) : cached;

		try {
			return BinaryMsdfSource.of(this.file).read();
		} catch (final IOException exception) {
			if (this.generated) {
				throw exception;
			}

			this.generated = true;
			OpenTypeMsdfSource.AREA.remove(key);
			this.file = OpenTypeMsdfSource.AREA.compute(key, OpenTypeMsdfSource.generator(font));
			return BinaryMsdfSource.of(this.file).read();
		}
	}

	private static @NonNull String key(final byte[] font) {
		return Hashing.sha256().newHasher().putString(JOID.VERSION + " " + MsdfWriter.VERSION + " " + MsdfGenerator.REVISION + " " + MsdfGenerator.CHARSET + " " + MsdfGenerator.RANGE + " " + MsdfGenerator.WIDTH + "x" + MsdfGenerator.HEIGHT, StandardCharsets.UTF_8).putBytes(font).hash() + ".msdf";
	}

	private static @NonNull FileCacheWriter generator(final byte[] font) {
		return file -> {
			try {
				MsdfGenerator.generate(font, file, MsdfGenerator.codepoints(MsdfGenerator.CHARSET), MsdfGenerator.WIDTH, MsdfGenerator.HEIGHT, MsdfGenerator.RANGE, 0D);
			} catch (final Exception exception) {
				throw new IOException("The msdf atlas of the font cannot be generated in " + file.getParentFile().getAbsolutePath() + ", check that the font is a valid TrueType or OpenType file and that the cache folder can be written", exception);
			}
		};
	}

}