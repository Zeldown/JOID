package dev.joid.lib.resource.dto.animation.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.Node;

import dev.joid.lib.resource.dto.animation.Animation;
import dev.joid.lib.resource.dto.animation.AnimationCanvas;
import dev.joid.lib.resource.dto.animation.AnimationCanvas.Blend;
import dev.joid.lib.resource.dto.animation.AnimationCanvas.Disposal;
import dev.joid.lib.resource.dto.animation.AnimationFrame;
import dev.joid.lib.resource.dto.animation.IAnimationReader;
import lombok.NonNull;

public class GifAnimationReader implements IAnimationReader {

	private static final String IMAGE_FORMAT  = "javax_imageio_gif_image_1.0";
	private static final String STREAM_FORMAT = "javax_imageio_gif_stream_1.0";

	@Override
	public @NonNull Animation read(final @NonNull InputStream stream) throws IOException {
		final Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
		if (!readers.hasNext()) {
			throw new IOException("No GIF reader available");
		}

		final ImageReader reader = readers.next();
		try (ImageInputStream input = ImageIO.createImageInputStream(stream)) {
			reader.setInput(input, false);
			final IIOMetadataNode screen = GifAnimationReader.child((IIOMetadataNode) reader.getStreamMetadata().getAsTree(GifAnimationReader.STREAM_FORMAT), "LogicalScreenDescriptor");
			final int count = reader.getNumImages(true);
			int width = screen == null ? 0 : Integer.parseInt(screen.getAttribute("logicalScreenWidth"));
			int height = screen == null ? 0 : Integer.parseInt(screen.getAttribute("logicalScreenHeight"));
			if (width <= 0 || height <= 0) {
				width = reader.getWidth(0);
				height = reader.getHeight(0);
			}

			int plays = 1;
			final AnimationCanvas canvas = AnimationCanvas.create(width, height);
			final List<AnimationFrame> frames = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				final BufferedImage image = reader.read(i);
				final IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(i).getAsTree(GifAnimationReader.IMAGE_FORMAT);
				final IIOMetadataNode descriptor = GifAnimationReader.child(root, "ImageDescriptor");
				final IIOMetadataNode control = GifAnimationReader.child(root, "GraphicControlExtension");
				plays = GifAnimationReader.plays(root, plays);

				final int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
				final int x = descriptor == null ? 0 : Integer.parseInt(descriptor.getAttribute("imageLeftPosition"));
				final int y = descriptor == null ? 0 : Integer.parseInt(descriptor.getAttribute("imageTopPosition"));
				final long delay = control == null ? 0L : Long.parseLong(control.getAttribute("delayTime")) * 10L;
				frames.add(AnimationFrame.create(canvas.compose(pixels, x, y, image.getWidth(), image.getHeight(), Blend.OVER, GifAnimationReader.disposal(control)), delay));
			}
			return Animation.create(width, height, plays, frames);
		} finally {
			reader.dispose();
		}
	}

	private static Disposal disposal(final IIOMetadataNode control) {
		if (control == null) {
			return Disposal.NONE;
		}

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
		final IIOMetadataNode extensions = GifAnimationReader.child(root, "ApplicationExtensions");
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