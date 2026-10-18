package dev.joid.demo.resource.format.impl;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import dev.joid.lib.resource.decoder.impl.RasterResourceDecoder;
import dev.joid.lib.resource.format.IResourceFormat;
import lombok.NonNull;

public final class DemoCheckerResourceFormat implements IResourceFormat {

	@Override
	public boolean supports(final @NonNull byte[] header) {
		return new String(header, StandardCharsets.US_ASCII).startsWith("CHECKER ");
	}

	@Override
	public @NonNull IResourceDecoder decoder(final @NonNull Asset asset, final @NonNull byte[] header) {
		final int cell = 8;
		final int cells = Integer.parseInt(new String(header, StandardCharsets.US_ASCII).substring(8).trim());
		final BufferedImage image = new BufferedImage(cells * cell, cells * cell, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				image.setRGB(x, y, (x / cell + y / cell) % 2 == 0 ? 0xFFDDDDDD : 0xFF999999);
			}
		}

		return RasterResourceDecoder.create(image);
	}

}