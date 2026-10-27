package com.example.further;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import dev.joid.lib.resource.animation.IResourceAnimationReader;
import dev.joid.lib.resource.animation.ResourceAnimation;
import dev.joid.lib.resource.animation.ResourceAnimationFrame;

// #region reader
public class FormatsStripAnimationReader implements IResourceAnimationReader {

	private final int  frameCount;
	private final long frameDuration;

	public FormatsStripAnimationReader(final int frameCount, final long frameDuration) {
		this.frameCount = frameCount;
		this.frameDuration = frameDuration;
	}

	@Override
	public ResourceAnimation read(final InputStream stream) throws IOException {
		final BufferedImage strip = ImageIO.read(stream);
		if (strip == null) {
			throw new IOException("The sprite strip is not an image that ImageIO can read");
		}

		final int width = strip.getWidth() / this.frameCount;
		final int height = strip.getHeight();
		final List<ResourceAnimationFrame> frames = new ArrayList<>();
		for (int i = 0; i < this.frameCount; i++) {
			final int[] pixels = strip.getRGB(i * width, 0, width, height, null, 0, width);
			frames.add(ResourceAnimationFrame.of(pixels, this.frameDuration));
		}
		return ResourceAnimation.of(width, height, 0, frames);
	}

}
// #endregion