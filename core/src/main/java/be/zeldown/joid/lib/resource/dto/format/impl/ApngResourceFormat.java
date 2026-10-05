package be.zeldown.joid.lib.resource.dto.format.impl;

import be.zeldown.joid.lib.asset.Asset;
import be.zeldown.joid.lib.resource.dto.animation.impl.ApngAnimationReader;
import be.zeldown.joid.lib.resource.dto.decoder.IResourceDecoder;
import be.zeldown.joid.lib.resource.dto.decoder.impl.AnimatedResourceDecoder;
import be.zeldown.joid.lib.resource.dto.decoder.impl.RasterResourceDecoder;
import be.zeldown.joid.lib.resource.dto.format.IResourceFormat;
import lombok.NonNull;

public class ApngResourceFormat implements IResourceFormat {

	private static final int CHUNKS = 1 << 16;

	@Override
	public boolean supports(final @NonNull byte[] header) {
		return header.length >= 8 && header[0] == (byte) 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G';
	}

	@Override
	public @NonNull IResourceDecoder decoder(final @NonNull Asset asset, final @NonNull byte[] header) {
		final boolean animated = ApngAnimationReader.isAnimated(header).orElseGet(() -> ApngAnimationReader.isAnimated(asset.peek(ApngResourceFormat.CHUNKS)).orElse(false));
		return animated ? new AnimatedResourceDecoder(asset, new ApngAnimationReader()) : new RasterResourceDecoder(asset);
	}

}