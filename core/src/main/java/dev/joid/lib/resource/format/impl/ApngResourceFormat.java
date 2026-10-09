package dev.joid.lib.resource.format.impl;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.resource.animation.impl.ApngResourceAnimationReader;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import dev.joid.lib.resource.decoder.impl.AnimatedResourceDecoder;
import dev.joid.lib.resource.decoder.impl.RasterResourceDecoder;
import dev.joid.lib.resource.format.IResourceFormat;
import lombok.NonNull;

public class ApngResourceFormat implements IResourceFormat {

	@Override
	public boolean supports(final @NonNull byte[] header) {
		return header.length >= 8 && header[0] == (byte) 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G';
	}

	@Override
	public @NonNull IResourceDecoder decoder(final @NonNull Asset asset, final @NonNull byte[] header) {
		final Boolean headerAnimated = ApngResourceAnimationReader.isAnimated(header);
		final boolean animated = headerAnimated != null ? headerAnimated : Boolean.TRUE.equals(ApngResourceAnimationReader.isAnimated(asset.peek(1 << 16)));
		return animated ? new AnimatedResourceDecoder(asset, new ApngResourceAnimationReader()) : new RasterResourceDecoder(asset);
	}

}