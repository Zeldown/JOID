package be.zeldown.joid.lib.resource.dto.decoder.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.resource.dto.ResourceData;
import be.zeldown.joid.lib.resource.dto.decoder.IResourceDecoder;
import be.zeldown.joid.lib.utils.texture.AllocatedTextureUtil;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class ImageResourceDecoder implements IResourceDecoder {

	private BufferedImage image;

	public ImageResourceDecoder(final @NonNull InputStream inputStream) {
		try {
			this.image = ImageIO.read(inputStream);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				inputStream.close();
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		if (this.image == null) {
			throw new RuntimeException("Failed to decode image, ImageIO returned null.");
		}
	}

	@Override
	public void init(final @NonNull ResourceData resource) {}

	@Override
	public void prepare(final @NonNull ResourceData resource) {
		resource.textureId(new int[] {GL11.glGenTextures()});
		AllocatedTextureUtil.allocateTexture(resource.getTextureId()[0], 1, 1);
		AllocatedTextureUtil.uploadTexture(resource.getTextureId()[0], new int[] {0}, 1, 1);
	}

	@Override
	public void decode(final @NonNull ResourceData resource) {
		resource.width(this.image.getWidth());
		resource.height(this.image.getHeight());

		resource.data(new int[1][resource.getWidth() * resource.getHeight()]);
		this.image.getRGB(0, 0, resource.getWidth(), resource.getHeight(), resource.getData()[0], 0, resource.getWidth());
	}

	@Override
	public void upload(final @NonNull ResourceData resource) {
		for (int i = 0; i < resource.getTextureId().length; i++) {
			if (resource.getData()[i] == null) {
				continue;
			}

			AllocatedTextureUtil.allocateTexture(resource.getTextureId()[i], resource.getWidth(), resource.getHeight());
			AllocatedTextureUtil.uploadTexture(resource.getTextureId()[i], resource.getData()[i], resource.getWidth(), resource.getHeight());
		}
		this.clear(resource);
	}

	@Override
	public void bind(final @NonNull ResourceData resource) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, resource.getTextureId()[0]);
	}

	@Override
	public void clear(final @NonNull ResourceData resource) {
		this.image = null;
	}

}
