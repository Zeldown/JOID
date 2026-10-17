package dev.joid.lib.resource.decoder.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import javax.imageio.ImageIO;
import javax.imageio.spi.ImageReaderSpi;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.resource.ResourceData;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import dev.joid.lib.utils.image.ImageUtils;
import lombok.NonNull;

public class RasterResourceDecoder implements IResourceDecoder {

	private final Asset          asset;
	private final BufferedImage  source;
	private final ImageReaderSpi reader;

	private BufferedImage image;

	protected RasterResourceDecoder(final Asset asset, final BufferedImage image, final ImageReaderSpi reader) {
		this.asset = asset;
		this.source = image;
		this.reader = reader;
	}

	public static @NonNull RasterResourceDecoder create(final @NonNull Asset asset) {
		return new RasterResourceDecoder(asset, null, null);
	}

	public static @NonNull RasterResourceDecoder create(final @NonNull BufferedImage image) {
		return new RasterResourceDecoder(null, image, null);
	}

	public static @NonNull RasterResourceDecoder create(final @NonNull Asset asset, final @NonNull ImageReaderSpi reader) {
		return new RasterResourceDecoder(asset, null, reader);
	}

	@Override
	public void prepare(final @NonNull ResourceData resource) {
		resource.texture(ResourceData.placeholder());
	}

	@Override
	public void decode(final @NonNull ResourceData resource) {
		if (this.image == null) {
			this.image = this.source != null ? this.source : RasterResourceDecoder.read(this.asset, this.reader);
		}

		resource.width(this.image.getWidth());
		resource.height(this.image.getHeight());

		resource.data(new int[1][resource.getWidth() * resource.getHeight()]);
		this.image.getRGB(0, 0, resource.getWidth(), resource.getHeight(), resource.getData(0), 0, resource.getWidth());
		ImageUtils.bleedAlpha(resource.getData(0), resource.getWidth(), resource.getHeight());
	}

	@Override
	public void upload(final @NonNull ResourceData resource) {
		resource.uploadTextures();
		this.clear(resource);
	}

	@Override
	public void clear(final @NonNull ResourceData resource) {
		this.image = null;
	}

	private static @NonNull BufferedImage read(final @NonNull Asset asset, final ImageReaderSpi reader) {
		final BufferedImage image;
		try (InputStream stream = asset.open()) {
			image = reader == null ? ImageIO.read(stream) : ImageUtils.read(stream, reader);
		} catch (final IOException exception) {
			throw new UncheckedIOException("The image " + asset.getUniqueId() + " cannot be read", exception);
		}

		if (image == null) {
			throw new IllegalArgumentException(asset.getUniqueId() + " is not an image that ImageIO can decode, convert it to PNG, JPEG or WebP");
		}
		return image;
	}

}