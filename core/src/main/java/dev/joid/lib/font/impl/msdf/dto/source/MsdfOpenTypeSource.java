package dev.joid.lib.font.impl.msdf.dto.source;

import java.io.IOException;
import java.util.Arrays;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.font.impl.msdf.MsdfFontCache;
import dev.joid.lib.font.impl.msdf.dto.MsdfFontFace;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MsdfOpenTypeSource extends MsdfSource {

	public static final int HEADER = 4;

	private static final byte[][] SIGNATURES = {{0, 1, 0, 0}, {'t', 'r', 'u', 'e'}, {'O', 'T', 'T', 'O'}, {'t', 't', 'c', 'f'}};

	private final Asset asset;

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
	protected @NonNull MsdfFontFace parse() throws IOException {
		return MsdfBinarySource.of(MsdfFontCache.resolve(this.asset.read())).read();
	}

}