package dev.joid.lib.resource.format.impl;

import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.resource.animation.impl.WebpResourceAnimationReader;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import dev.joid.lib.resource.decoder.impl.AnimatedResourceDecoder;
import dev.joid.lib.resource.decoder.impl.RasterResourceDecoder;
import dev.joid.lib.resource.format.IResourceFormat;
import lombok.NonNull;

public class WebpResourceFormat implements IResourceFormat {

	@Override
	public boolean supports(final @NonNull byte[] header) {
		return WebpResourceAnimationReader.isWebp(header);
	}

	@Override
	public @NonNull IResourceDecoder decoder(final @NonNull Asset asset, final @NonNull byte[] header) {
		return WebpResourceAnimationReader.isAnimated(header) ? new AnimatedResourceDecoder(asset, new WebpResourceAnimationReader()) : new RasterResourceDecoder(asset, new WebPImageReaderSpi());
	}

}