package dev.joid.lib.resource.dto.animation;

import dev.joid.lib.utils.image.ImageUtils;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ResourceAnimationCanvas {

	private final int   width;
	private final int   height;
	private final int[] pixels;

	private ResourceAnimationCanvas(final int width, final int height) {
		this.width = width;
		this.height = height;
		this.pixels = new int[width * height];
	}

	public static @NonNull ResourceAnimationCanvas create(final int width, final int height) {
		return new ResourceAnimationCanvas(width, height);
	}

	public @NonNull int[] compose(final @NonNull int[] frame, final int x, final int y, final int frameWidth, final int frameHeight, final @NonNull Blend blend, final @NonNull Disposal disposal) {
		final int[] previous = disposal == Disposal.PREVIOUS ? this.pixels.clone() : null;
		final int left = Math.max(0, x);
		final int top = Math.max(0, y);
		final int right = Math.min(this.width, x + frameWidth);
		final int bottom = Math.min(this.height, y + frameHeight);
		for (int row = top; row < bottom; row++) {
			for (int column = left; column < right; column++) {
				final int source = frame[(row - y) * frameWidth + column - x];
				final int index = row * this.width + column;
				this.pixels[index] = blend == Blend.SOURCE ? source : ResourceAnimationCanvas.over(source, this.pixels[index]);
			}
		}

		final int[] composed = this.pixels.clone();
		ImageUtils.bleedAlpha(composed, this.width, this.height);
		if (disposal == Disposal.PREVIOUS) {
			System.arraycopy(previous, 0, this.pixels, 0, this.pixels.length);
		} else if (disposal == Disposal.BACKGROUND) {
			for (int row = top; row < bottom; row++) {
				for (int column = left; column < right; column++) {
					this.pixels[row * this.width + column] = 0;
				}
			}
		}
		return composed;
	}

	private static int over(final int source, final int destination) {
		final int sourceAlpha = source >>> 24;
		if (sourceAlpha == 255) {
			return source;
		}

		if (sourceAlpha == 0) {
			return destination;
		}

		final int destinationAlpha = (destination >>> 24) * (255 - sourceAlpha) / 255;
		final int alpha = sourceAlpha + destinationAlpha;
		final int red = ((source >> 16 & 0xFF) * sourceAlpha + (destination >> 16 & 0xFF) * destinationAlpha) / alpha;
		final int green = ((source >> 8 & 0xFF) * sourceAlpha + (destination >> 8 & 0xFF) * destinationAlpha) / alpha;
		final int blue = ((source & 0xFF) * sourceAlpha + (destination & 0xFF) * destinationAlpha) / alpha;
		return alpha << 24 | red << 16 | green << 8 | blue;
	}

	public enum Blend {

		SOURCE,
		OVER;

	}

	public enum Disposal {

		NONE,
		BACKGROUND,
		PREVIOUS;

	}

}