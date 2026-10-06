package dev.joid.lib.resource.dto.animation.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.Node;

import dev.joid.lib.resource.dto.animation.IResourceAnimationReader;
import dev.joid.lib.resource.dto.animation.ResourceAnimation;
import dev.joid.lib.resource.dto.animation.ResourceAnimationCanvas;
import dev.joid.lib.resource.dto.animation.ResourceAnimationCanvas.Blend;
import dev.joid.lib.resource.dto.animation.ResourceAnimationCanvas.Disposal;
import dev.joid.lib.resource.dto.animation.ResourceAnimationFrame;
import lombok.NonNull;

public class GifResourceAnimationReader implements IResourceAnimationReader {

	@Override
	public @NonNull ResourceAnimation read(final @NonNull InputStream stream) throws IOException {
		final ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
		try (ImageInputStream input = ImageIO.createImageInputStream(stream)) {
			reader.setInput(input, false);
			final IIOMetadataNode screen = GifResourceAnimationReader.child((IIOMetadataNode) reader.getStreamMetadata().getAsTree("javax_imageio_gif_stream_1.0"), "LogicalScreenDescriptor");
			final int count = reader.getNumImages(true);
			int width = screen == null ? 0 : Integer.parseInt(screen.getAttribute("logicalScreenWidth"));
			int height = screen == null ? 0 : Integer.parseInt(screen.getAttribute("logicalScreenHeight"));
			if (width <= 0 || height <= 0) {
				width = reader.getWidth(0);
				height = reader.getHeight(0);
			}

			int plays = 1;
			final ResourceAnimationCanvas canvas = ResourceAnimationCanvas.create(width, height);
			final List<ResourceAnimationFrame> frames = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				final BufferedImage image = reader.read(i);
				final IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(i).getAsTree("javax_imageio_gif_image_1.0");
				final IIOMetadataNode descriptor = GifResourceAnimationReader.child(root, "ImageDescriptor");
				final IIOMetadataNode control = GifResourceAnimationReader.child(root, "GraphicControlExtension");
				plays = GifResourceAnimationReader.plays(root, plays);

				final int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
				final int x = descriptor == null ? 0 : Integer.parseInt(descriptor.getAttribute("imageLeftPosition"));
				final int y = descriptor == null ? 0 : Integer.parseInt(descriptor.getAttribute("imageTopPosition"));
				final long delay = Long.parseLong(control.getAttribute("delayTime")) * 10L;
				frames.add(ResourceAnimationFrame.create(canvas.compose(pixels, x, y, image.getWidth(), image.getHeight(), Blend.OVER, GifResourceAnimationReader.disposal(control)), delay));
			}
			return ResourceAnimation.create(width, height, plays, frames);
		} finally {
			reader.dispose();
		}
	}

	private static Disposal disposal(final IIOMetadataNode control) {
		switch (control.getAttribute("disposalMethod")) {
		case "restoreToBackgroundColor":
			return Disposal.BACKGROUND;
		case "restoreToPrevious":
			return Disposal.PREVIOUS;
		default:
			return Disposal.NONE;
		}
	}

	private static int plays(final IIOMetadataNode root, final int fallback) {
		final IIOMetadataNode extensions = GifResourceAnimationReader.child(root, "ApplicationExtensions");
		if (extensions == null) {
			return fallback;
		}

		for (Node node = extensions.getFirstChild(); node != null; node = node.getNextSibling()) {
			final IIOMetadataNode extension = (IIOMetadataNode) node;
			if ("NETSCAPE".equals(extension.getAttribute("applicationID")) && extension.getUserObject() instanceof byte[]) {
				final byte[] data = (byte[]) extension.getUserObject();
				if (data.length >= 3 && data[0] == 1) {
					final int loops = (data[1] & 0xFF) | (data[2] & 0xFF) << 8;
					return loops == 0 ? 0 : loops + 1;
				}
			}
		}
		return fallback;
	}

	private static IIOMetadataNode child(final IIOMetadataNode parent, final String name) {
		for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getNodeName().equals(name)) {
				return (IIOMetadataNode) node;
			}
		}
		return null;
	}

}