package dev.joid.lib.resource.format.impl;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import dev.joid.lib.resource.format.IResourceFormat;
import lombok.NonNull;

public class HeifResourceFormat implements IResourceFormat {

	@Override
	public boolean supports(final @NonNull byte[] header) {
		return HeifResourceFormat.isHeif(header);
	}

	@Override
	public @NonNull IResourceDecoder decoder(final @NonNull Asset asset, final @NonNull byte[] header) {
		throw new IllegalArgumentException(asset.getUniqueId() + " is a HEIF or AVIF image, which JOID cannot decode");
	}

	public static boolean isHeif(final @NonNull byte[] header) {
		if (header.length < 12 || header[4] != 'f' || header[5] != 't' || header[6] != 'y' || header[7] != 'p') {
			return false;
		}
		return Arrays.asList("avif", "avis", "heic", "heim", "heis", "heix", "hevc", "hevm", "hevs", "hevx", "mif1", "msf1").contains(new String(header, 8, 4, StandardCharsets.ISO_8859_1));
	}

}