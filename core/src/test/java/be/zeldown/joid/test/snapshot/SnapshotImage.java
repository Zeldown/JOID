package be.zeldown.joid.test.snapshot;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.imageio.ImageIO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SnapshotImage {

	private final int   width;
	private final int   height;
	private final int[] pixels;

	public static @NonNull SnapshotImage fromBytes(final @NonNull ByteBuffer buffer, final int width, final int height, final boolean bottomUp, final boolean bgra) {
		final int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			final int row = bottomUp ? height - 1 - y : y;
			for (int x = 0; x < width; x++) {
				final int index = (x + row * width) * 4;
				final int first = buffer.get(index) & 0xFF;
				final int second = buffer.get(index + 1) & 0xFF;
				final int third = buffer.get(index + 2) & 0xFF;
				pixels[x + y * width] = 0xFF000000 | (bgra ? third << 16 | second << 8 | first : first << 16 | second << 8 | third);
			}
		}
		return new SnapshotImage(width, height, pixels);
	}

	public static @NonNull SnapshotImage read(final @NonNull File file) {
		try {
			final BufferedImage image = ImageIO.read(file);
			return new SnapshotImage(image.getWidth(), image.getHeight(), image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth()));
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public void write(final @NonNull File file) {
		file.getParentFile().mkdirs();
		final BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 0, this.width, this.height, this.pixels, 0, this.width);
		try {
			ImageIO.write(image, "png", file);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public boolean isSame(final @NonNull SnapshotImage other) {
		return this.width == other.getWidth() && this.height == other.getHeight() && Arrays.equals(this.pixels, other.getPixels());
	}

	public @NonNull SnapshotDifference compare(final @NonNull SnapshotImage reference, final int tolerance) {
		if (this.width != reference.getWidth() || this.height != reference.getHeight()) {
			return SnapshotDifference.create(this.pixels.length, 255, this);
		}

		final int[] expected = reference.getPixels();
		final int[] difference = new int[this.pixels.length];
		int pixels = 0;
		int maximum = 0;
		for (int i = 0; i < this.pixels.length; i++) {
			final int red = Math.abs((this.pixels[i] >> 16 & 0xFF) - (expected[i] >> 16 & 0xFF));
			final int green = Math.abs((this.pixels[i] >> 8 & 0xFF) - (expected[i] >> 8 & 0xFF));
			final int blue = Math.abs((this.pixels[i] & 0xFF) - (expected[i] & 0xFF));
			final int delta = Math.max(red, Math.max(green, blue));
			maximum = Math.max(maximum, delta);

			if (delta > tolerance) {
				pixels++;
				difference[i] = 0xFFFF0000;
			} else {
				final int gray = ((expected[i] >> 16 & 0xFF) + (expected[i] >> 8 & 0xFF) + (expected[i] & 0xFF)) / 9;
				difference[i] = 0xFF000000 | gray << 16 | gray << 8 | gray;
			}
		}
		return SnapshotDifference.create(pixels, maximum, new SnapshotImage(this.width, this.height, difference));
	}

}