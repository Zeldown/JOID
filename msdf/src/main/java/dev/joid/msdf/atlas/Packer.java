package dev.joid.msdf.atlas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Packer {

	public static boolean pack(final List<GlyphEntry> glyphs, final double size, final double range, final int width, final int height) {
		final List<GlyphEntry> drawable = new ArrayList<>();
		for (final GlyphEntry glyph : glyphs) {
			if (glyph.isDrawable()) {
				glyph.measure(size, range);
				drawable.add(glyph);
			}
		}

		drawable.sort(Comparator.comparingInt(GlyphEntry::getHeight).thenComparingInt(GlyphEntry::getWidth).reversed().thenComparingInt(GlyphEntry::getCodepoint));

		final int[] skyline = new int[width];
		for (final GlyphEntry glyph : drawable) {
			final int glyphWidth = glyph.getWidth() + 1;
			final int glyphHeight = glyph.getHeight() + 1;
			if (glyphWidth > width) {
				return false;
			}

			final int x = Packer.lowest(skyline, glyphWidth);
			final int y = Packer.highest(skyline, x, glyphWidth);
			if (y + glyphHeight > height) {
				return false;
			}

			glyph.setX(x);
			glyph.setY(y);
			for (int column = x; column < x + glyphWidth; column++) {
				skyline[column] = y + glyphHeight;
			}
		}

		return true;
	}

	public static double fit(final List<GlyphEntry> glyphs, final double range, final int width, final int height) {
		double low = 1D;
		double high = height;
		while (high - low > 0.0625D) {
			final double middle = Math.floor((low + high) / 2D * 16D) / 16D;
			if (middle <= low) {
				break;
			}

			if (Packer.pack(glyphs, middle, range, width, height)) {
				low = middle;
			} else {
				high = middle;
			}
		}

		Packer.pack(glyphs, low, range, width, height);
		return low;
	}

	private static int lowest(final int[] skyline, final int window) {
		final int[] deque = new int[skyline.length];
		int head = 0;
		int tail = 0;
		int best = 0;
		int bestHeight = Integer.MAX_VALUE;

		for (int x = 0; x < skyline.length; x++) {
			while (tail > head && skyline[deque[tail - 1]] <= skyline[x]) {
				tail--;
			}
			deque[tail++] = x;

			final int start = x - window + 1;
			if (start < 0) {
				continue;
			}

			while (deque[head] < start) {
				head++;
			}

			if (skyline[deque[head]] < bestHeight) {
				bestHeight = skyline[deque[head]];
				best = start;
			}
		}

		return best;
	}

	private static int highest(final int[] skyline, final int start, final int window) {
		int highest = 0;
		for (int x = start; x < start + window && x < skyline.length; x++) {
			highest = Math.max(highest, skyline[x]);
		}
		return highest;
	}

}