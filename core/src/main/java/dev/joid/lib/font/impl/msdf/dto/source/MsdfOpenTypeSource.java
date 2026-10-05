package dev.joid.lib.font.impl.msdf.dto.source;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.font.impl.msdf.MsdfFontCache;
import dev.joid.lib.font.impl.msdf.dto.MsdfFontFace;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MsdfOpenTypeSource extends MsdfSource {

	public static final int HEADER = 4;

	private static final byte[][] SIGNATURES = {{0, 1, 0, 0}, {'t', 'r', 'u', 'e'}, {'O', 'T', 'T', 'O'}, {'t', 't', 'c', 'f'}};

	private final Asset asset;

	private File    file;
	private boolean generated;

	public static @NonNull MsdfOpenTypeSource of(final @NonNull Object handle) {
		return new MsdfOpenTypeSource(Asset.of(handle));
	}

	public static boolean supports(final @NonNull byte[] header) {
		for (final byte[] signature : MsdfOpenTypeSource.SIGNATURES) {
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
		this.file = MsdfFontCache.locate(font);
		this.generated = !this.file.isFile();
		if (this.generated) {
			MsdfFontCache.generate(font, this.file);
		}
		return MsdfBinarySource.of(this.file).read();
	}

}