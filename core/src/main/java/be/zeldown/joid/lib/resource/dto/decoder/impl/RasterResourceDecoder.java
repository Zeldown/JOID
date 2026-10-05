package be.zeldown.joid.lib.resource.dto.decoder.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import be.zeldown.joid.lib.asset.Asset;
import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.resource.dto.ResourceData;
import be.zeldown.joid.lib.resource.dto.decoder.IResourceDecoder;
import be.zeldown.joid.lib.utils.image.ImageUtils;
import lombok.NonNull;

public class RasterResourceDecoder implements IResourceDecoder {

	private final Asset asset;

	private BufferedImage image;

	public RasterResourceDecoder(final @NonNull Asset asset) {
		this.asset = asset;
	}

	public RasterResourceDecoder(final @NonNull BufferedImage image) {
		this.asset = null;
		this.image = image;
	}

	@Override
	public void init(final @NonNull ResourceData resource) {}

	@Override
	public void prepare(final @NonNull ResourceData resource) {
		resource.texture(BridgeHandler.RENDER.get().createTexture().allocate(1, 1).upload(new int[] {0}, 1, 1));
	}

	@Override
	public void decode(final @NonNull ResourceData resource) {
		if (this.image == null) {
			this.image = RasterResourceDecoder.read(this.asset);
		}

		resource.width(this.image.getWidth());
		resource.height(this.image.getHeight());

		resource.data(new int[1][resource.getWidth() * resource.getHeight()]);
		this.image.getRGB(0, 0, resource.getWidth(), resource.getHeight(), resource.getData()[0], 0, resource.getWidth());
		ImageUtils.bleedAlpha(resource.getData()[0], resource.getWidth(), resource.getHeight());
	}

	@Override
	public void upload(final @NonNull ResourceData resource) {
		for (int i = 0; i < resource.getTextures().length; i++) {
			if (resource.getData()[i] == null) {
				continue;
			}

			resource.getTextures()[i].allocate(resource.getWidth(), resource.getHeight()).upload(resource.getData()[i], resource.getWidth(), resource.getHeight());
		}
		this.clear(resource);
	}

	@Override
	public void update(final @NonNull ResourceData resource) {}

	@Override
	public void clear(final @NonNull ResourceData resource) {
		this.image = null;
	}

	private static @NonNull BufferedImage read(final @NonNull Asset asset) {
		final BufferedImage image;
		try (InputStream stream = asset.open()) {
			image = ImageIO.read(stream);
		} catch (final IOException exception) {
			throw new RuntimeException("Unable to read the image of " + asset.getUniqueId(), exception);
		}

		if (image == null) {
			throw new RuntimeException("Failed to decode image, ImageIO returned null for " + asset.getUniqueId());
		}
		return image;
	}

}