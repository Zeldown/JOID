package be.zeldown.joid.lib.resource.dto.format.impl;

import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;

import be.zeldown.joid.lib.asset.Asset;
import be.zeldown.joid.lib.resource.dto.animation.impl.WebpAnimationReader;
import be.zeldown.joid.lib.resource.dto.decoder.IResourceDecoder;
import be.zeldown.joid.lib.resource.dto.decoder.impl.AnimatedResourceDecoder;
import be.zeldown.joid.lib.resource.dto.decoder.impl.RasterResourceDecoder;
import be.zeldown.joid.lib.resource.dto.format.IResourceFormat;
import lombok.NonNull;

public class WebpResourceFormat implements IResourceFormat {

	@Override
	public boolean supports(final @NonNull byte[] header) {
		return WebpAnimationReader.isWebp(header);
	}

	@Override
	public @NonNull IResourceDecoder decoder(final @NonNull Asset asset, final @NonNull byte[] header) {
		return WebpAnimationReader.isAnimated(header) ? new AnimatedResourceDecoder(asset, new WebpAnimationReader()) : new RasterResourceDecoder(asset, new WebPImageReaderSpi());
	}

}