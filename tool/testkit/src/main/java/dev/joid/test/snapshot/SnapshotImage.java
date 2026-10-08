package dev.joid.test.snapshot;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.imageio.ImageIO;

import dev.joid.lib.utils.image.PixelLayout;
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

	public static @NonNull SnapshotImage read(final @NonNull File file) {
		try {
			final BufferedImage image = ImageIO.read(file);
			if (image == null) {
				throw new IOException("Unreadable image " + file);
			}

			return new SnapshotImage(image.getWidth(), image.getHeight(), image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth()));
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static @NonNull SnapshotImage fromBytes(final @NonNull ByteBuffer buffer, final int width, final int height, final boolean bottomUp, final @NonNull PixelLayout layout) {
		final int[] pixels = layout.read(buffer, width, height, bottomUp);
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] |= 0xFF000000;
		}
		return new SnapshotImage(width, height, pixels);
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

	public void fill(final int x, final int y, final int width, final int height, final int color) {
		for (int row = Math.max(0, y); row < Math.min(this.height, y + height); row++) {
			for (int column = Math.max(0, x); column < Math.min(this.width, x + width); column++) {
				this.pixels[column + row * this.width] = color;
			}
		}
	}

	public @NonNull SnapshotDifference compare(final @NonNull SnapshotImage reference, final int tolerance) {
		if (this.width != reference.getWidth() || this.height != reference.getHeight()) {
			return SnapshotDifference.create(this.pixels.length, 255);
		}

		final int[] expected = reference.getPixels();
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
			}
		}
		return SnapshotDifference.create(pixels, maximum);
	}

}