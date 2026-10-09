package dev.joid.lib.resource.animation.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.apache.commons.io.IOUtils;

import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;

import dev.joid.lib.resource.animation.IResourceAnimationReader;
import dev.joid.lib.resource.animation.ResourceAnimation;
import dev.joid.lib.resource.animation.ResourceAnimationCanvas;
import dev.joid.lib.resource.animation.ResourceAnimationCanvas.Blend;
import dev.joid.lib.resource.animation.ResourceAnimationCanvas.Disposal;
import dev.joid.lib.resource.animation.ResourceAnimationFrame;
import lombok.NonNull;

public class WebpResourceAnimationReader implements IResourceAnimationReader {

	@Override
	public @NonNull ResourceAnimation read(final @NonNull InputStream stream) throws IOException {
		final byte[] bytes = IOUtils.toByteArray(stream);
		if (!WebpResourceAnimationReader.isWebp(bytes)) {
			throw new IOException("Not a WebP file");
		}

		int width = 0;
		int height = 0;
		int plays = 0;
		final List<Control> frames = new ArrayList<>();
		int offset = 12;
		while (offset + 8 <= bytes.length) {
			final String name = new String(bytes, offset, 4, StandardCharsets.US_ASCII);
			final int size = WebpResourceAnimationReader.read(bytes, offset + 4, 4);
			final int data = offset + 8;
			switch (name) {
			case "VP8X":
				width = WebpResourceAnimationReader.read(bytes, data + 4, 3) + 1;
				height = WebpResourceAnimationReader.read(bytes, data + 7, 3) + 1;
				break;
			case "ANIM":
				plays = WebpResourceAnimationReader.read(bytes, data + 4, 2);
				break;
			case "ANMF":
				frames.add(Control.read(bytes, data));
				break;
			default:
				break;
			}
			offset = data + size + (size & 1);
		}

		if (frames.isEmpty() || width <= 0 || height <= 0) {
			throw new IOException("Not an animated WebP");
		}

		final ImageReader reader = new WebPImageReaderSpi().createReaderInstance(null);
		try (MemoryCacheImageInputStream input = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
			reader.setInput(input);
			final ResourceAnimationCanvas canvas = ResourceAnimationCanvas.create(width, height);
			final List<ResourceAnimationFrame> animation = new ArrayList<>();
			for (int i = 0; i < frames.size(); i++) {
				final Control control = frames.get(i);
				final BufferedImage image = reader.read(i);
				final int[] pixels = image.getRGB(0, 0, control.width, control.height, null, 0, control.width);
				animation.add(ResourceAnimationFrame.create(canvas.compose(pixels, control.x, control.y, control.width, control.height, control.blend, control.disposal), control.duration));
			}
			return ResourceAnimation.create(width, height, plays, animation);
		} finally {
			reader.dispose();
		}
	}

	public static boolean isWebp(final @NonNull byte[] bytes) {
		return bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F' && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
	}

	public static boolean isAnimated(final @NonNull byte[] bytes) {
		return WebpResourceAnimationReader.isWebp(bytes) && bytes.length >= 21 && bytes[12] == 'V' && bytes[13] == 'P' && bytes[14] == '8' && bytes[15] == 'X' && (bytes[20] & 0x02) != 0;
	}

	private static int read(final byte[] bytes, final int offset, final int length) {
		int value = 0;
		for (int i = length - 1; i >= 0; i--) {
			value = value << 8 | bytes[offset + i] & 0xFF;
		}
		return value;
	}

	private static final class Control {

		private int      x;
		private int      y;
		private int      width;
		private int      height;
		private Blend    blend;
		private long     duration;
		private Disposal disposal;

		private static Control read(final byte[] bytes, final int offset) {
			final Control control = new Control();
			control.x = WebpResourceAnimationReader.read(bytes, offset, 3) * 2;
			control.y = WebpResourceAnimationReader.read(bytes, offset + 3, 3) * 2;
			control.width = WebpResourceAnimationReader.read(bytes, offset + 6, 3) + 1;
			control.height = WebpResourceAnimationReader.read(bytes, offset + 9, 3) + 1;
			control.duration = WebpResourceAnimationReader.read(bytes, offset + 12, 3);
			control.blend = (bytes[offset + 15] & 0x02) == 0 ? Blend.OVER : Blend.SOURCE;
			control.disposal = (bytes[offset + 15] & 0x01) == 0 ? Disposal.NONE : Disposal.BACKGROUND;
			return control;
		}

	}

}