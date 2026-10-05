package dev.joid.lib.utils.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.MemoryCacheImageInputStream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImageUtils {

	public static @NonNull BufferedImage read(final @NonNull InputStream stream, final @NonNull ImageReaderSpi spi) throws IOException {
		final ImageReader reader = spi.createReaderInstance(null);
		try (MemoryCacheImageInputStream input = new MemoryCacheImageInputStream(stream)) {
			reader.setInput(input);
			return reader.read(0);
		} finally {
			reader.dispose();
		}
	}

	public static void bleedAlpha(final @NonNull int[] pixels, final int width, final int height) {
		final int[] queue = new int[pixels.length];
		final boolean[] filled = new boolean[pixels.length];
		int tail = 0;
		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] >>> 24 != 0) {
				filled[i] = true;
				queue[tail++] = i;
			}
		}

		if (tail == 0 || tail == pixels.length) {
			return;
		}

		for (int head = 0; head < tail; head++) {
			final int index = queue[head];
			final int x = index % width;
			final int y = index / width;
			final int color = pixels[index] & 0xFFFFFF;
			for (int side = 0; side < 4; side++) {
				final int nx = x + (side == 0 ? -1 : side == 1 ? 1 : 0);
				final int ny = y + (side == 2 ? -1 : side == 3 ? 1 : 0);
				if (nx < 0 || ny < 0 || nx >= width || ny >= height) {
					continue;
				}

				final int neighbor = nx + ny * width;
				if (!filled[neighbor]) {
					filled[neighbor] = true;
					pixels[neighbor] = color;
					queue[tail++] = neighbor;
				}
			}
		}
	}

}