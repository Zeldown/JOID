package be.zeldown.joid.lib.resource.dto.animation.impl;

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

import be.zeldown.joid.lib.resource.dto.animation.Animation;
import be.zeldown.joid.lib.resource.dto.animation.AnimationCanvas;
import be.zeldown.joid.lib.resource.dto.animation.AnimationCanvas.Blend;
import be.zeldown.joid.lib.resource.dto.animation.AnimationCanvas.Disposal;
import be.zeldown.joid.lib.resource.dto.animation.AnimationFrame;
import be.zeldown.joid.lib.resource.dto.animation.IAnimationReader;
import lombok.NonNull;

public class WebpAnimationReader implements IAnimationReader {

	private static final int ANIMATION = 0x02;

	@Override
	public @NonNull Animation read(final @NonNull InputStream stream) throws IOException {
		final byte[] bytes = IOUtils.toByteArray(stream);
		if (!WebpAnimationReader.isWebp(bytes)) {
			throw new IOException("Not a WebP file");
		}

		int width = 0;
		int height = 0;
		int plays = 0;
		final List<Control> frames = new ArrayList<>();
		int offset = 12;
		while (offset + 8 <= bytes.length) {
			final String name = new String(bytes, offset, 4, StandardCharsets.US_ASCII);
			final int size = WebpAnimationReader.read(bytes, offset + 4, 4);
			final int data = offset + 8;
			switch (name) {
			case "VP8X":
				width = WebpAnimationReader.read(bytes, data + 4, 3) + 1;
				height = WebpAnimationReader.read(bytes, data + 7, 3) + 1;
				break;
			case "ANIM":
				plays = WebpAnimationReader.read(bytes, data + 4, 2);
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
			final AnimationCanvas canvas = AnimationCanvas.create(width, height);
			final List<AnimationFrame> animation = new ArrayList<>();
			for (int i = 0; i < frames.size(); i++) {
				final Control control = frames.get(i);
				final BufferedImage image = reader.read(i);
				final int[] pixels = image.getRGB(0, 0, control.width, control.height, null, 0, control.width);
				animation.add(AnimationFrame.create(canvas.compose(pixels, control.x, control.y, control.width, control.height, control.blend, control.disposal), control.duration));
			}
			return Animation.create(width, height, plays, animation);
		} finally {
			reader.dispose();
		}
	}

	public static boolean isWebp(final @NonNull byte[] bytes) {
		return bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F' && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
	}

	public static boolean isAnimated(final @NonNull byte[] bytes) {
		return WebpAnimationReader.isWebp(bytes) && bytes.length >= 21 && bytes[12] == 'V' && bytes[13] == 'P' && bytes[14] == '8' && bytes[15] == 'X' && (bytes[20] & WebpAnimationReader.ANIMATION) != 0;
	}

	private static int read(final byte[] bytes, final int offset, final int length) {
		int value = 0;
		for (int i = length - 1; i >= 0; i--) {
			value = value << 8 | bytes[offset + i] & 0xFF;
		}
		return value;
	}

	private static final class Control {

		private int      width;
		private int      height;
		private int      x;
		private int      y;
		private long     duration;
		private Blend    blend;
		private Disposal disposal;

		private static Control read(final byte[] bytes, final int offset) {
			final Control control = new Control();
			control.x = WebpAnimationReader.read(bytes, offset, 3) * 2;
			control.y = WebpAnimationReader.read(bytes, offset + 3, 3) * 2;
			control.width = WebpAnimationReader.read(bytes, offset + 6, 3) + 1;
			control.height = WebpAnimationReader.read(bytes, offset + 9, 3) + 1;
			control.duration = WebpAnimationReader.read(bytes, offset + 12, 3);
			control.blend = (bytes[offset + 15] & 0x02) == 0 ? Blend.OVER : Blend.SOURCE;
			control.disposal = (bytes[offset + 15] & 0x01) == 0 ? Disposal.NONE : Disposal.BACKGROUND;
			return control;
		}

	}

}