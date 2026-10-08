package dev.joid.lib.utils.image;

import java.nio.ByteBuffer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PixelLayout {

	RGBA8(16, 0),
	BGRA8(0, 16);

	private final int firstShift;
	private final int thirdShift;

	public @NonNull ByteBuffer write(final @NonNull int[] pixels, final @NonNull ByteBuffer target) {
		final int start = target.position();
		for (int i = 0; i < pixels.length; i++) {
			final int index = start + i * 4;
			target.put(index, (byte) (pixels[i] >> this.firstShift));
			target.put(index + 1, (byte) (pixels[i] >> 8));
			target.put(index + 2, (byte) (pixels[i] >> this.thirdShift));
			target.put(index + 3, (byte) (pixels[i] >>> 24));
		}
		return target;
	}

	public @NonNull int[] read(final @NonNull ByteBuffer source, final int width, final int height, final boolean bottomUp) {
		final int[] pixels = new int[width * height];
		final int start = source.position();
		for (int y = 0; y < height; y++) {
			final int row = bottomUp ? height - 1 - y : y;
			for (int x = 0; x < width; x++) {
				final int index = start + (x + row * width) * 4;
				pixels[x + y * width] = (source.get(index + 3) & 0xFF) << 24 | (source.get(index) & 0xFF) << this.firstShift | (source.get(index + 1) & 0xFF) << 8 | (source.get(index + 2) & 0xFF) << this.thirdShift;
			}
		}
		return pixels;
	}

}