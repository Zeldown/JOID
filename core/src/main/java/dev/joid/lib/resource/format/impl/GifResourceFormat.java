package dev.joid.lib.resource.format.impl;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.resource.animation.impl.GifResourceAnimationReader;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import dev.joid.lib.resource.decoder.impl.AnimatedResourceDecoder;
import dev.joid.lib.resource.format.IResourceFormat;
import lombok.NonNull;

public class GifResourceFormat implements IResourceFormat {

	@Override
	public boolean supports(final @NonNull byte[] header) {
		return header.length >= 6 && header[0] == 'G' && header[1] == 'I' && header[2] == 'F' && header[3] == '8' && (header[4] == '7' || header[4] == '9') && header[5] == 'a';
	}

	@Override
	public @NonNull IResourceDecoder decoder(final @NonNull Asset asset, final @NonNull byte[] header) {
		return new AnimatedResourceDecoder(asset, new GifResourceAnimationReader());
	}

}