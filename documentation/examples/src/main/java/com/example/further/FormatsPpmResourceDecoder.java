package com.example.further;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.resource.ResourceData;
import dev.joid.lib.resource.decoder.IResourceDecoder;

// #region decoder
public class FormatsPpmResourceDecoder implements IResourceDecoder {

	private final Asset asset;

	protected FormatsPpmResourceDecoder(final Asset asset) {
		this.asset = asset;
	}

	public static FormatsPpmResourceDecoder create(final Asset asset) {
		return new FormatsPpmResourceDecoder(asset);
	}

	@Override
	public void prepare(final ResourceData resource) {
		resource.texture(ResourceData.placeholder());
	}

	@Override
	public void decode(final ResourceData resource) {
		final String text;
		try {
			text = new String(this.asset.read(), StandardCharsets.US_ASCII);
		} catch (final IOException exception) {
			throw new UncheckedIOException("The image " + this.asset.getUniqueId() + " cannot be read", exception);
		}

		final String[] tokens = text.replaceAll("#[^\n]*", " ").trim().split("\\s+");
		final int width = Integer.parseInt(tokens[1]);
		final int height = Integer.parseInt(tokens[2]);
		final int max = Integer.parseInt(tokens[3]);
		final int[] pixels = new int[width * height];
		for (int i = 0; i < pixels.length; i++) {
			final int red = Integer.parseInt(tokens[4 + i * 3]) * 255 / max;
			final int green = Integer.parseInt(tokens[5 + i * 3]) * 255 / max;
			final int blue = Integer.parseInt(tokens[6 + i * 3]) * 255 / max;
			pixels[i] = 0xFF000000 | red << 16 | green << 8 | blue;
		}
		resource.width(width).height(height).data(new int[][] {pixels});
	}

	@Override
	public void upload(final ResourceData resource) {
		resource.uploadTextures();
	}

	@Override
	public void clear(final ResourceData resource) {}

}
// #endregion