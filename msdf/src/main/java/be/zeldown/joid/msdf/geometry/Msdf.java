package be.zeldown.joid.msdf.geometry;

import java.util.ArrayList;
import java.util.List;

public final class Msdf {

	public static int[] generate(final Shape shape, final int width, final int height, final double size, final double left, final double top, final double range) {
		final List<Edge> edges = new ArrayList<>();
		for (final List<Edge> contour : shape.getContours()) {
			edges.addAll(contour);
		}

		final double[] field = new double[width * height * 3];
		final double[] truth = new double[width * height];
		final SignedDistance candidate = new SignedDistance();
		final SignedDistance shortest = new SignedDistance();
		final SignedDistance[] closest = {new SignedDistance(), new SignedDistance(), new SignedDistance()};

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final double px = left + (x + 0.5D) / size;
				final double py = top - (y + 0.5D) / size;

				shortest.reset();
				closest[0].reset();
				closest[1].reset();
				closest[2].reset();

				for (final Edge edge : edges) {
					edge.distance(px, py, candidate);
					if (candidate.closerThan(shortest)) {
						shortest.copy(candidate);
					}

					for (int channel = 0; channel < 3; channel++) {
						if ((edge.getColor() & 1 << channel) != 0 && candidate.closerThan(closest[channel])) {
							closest[channel].copy(candidate);
							closest[channel].setEdge(edge);
						}
					}
				}

				truth[x + y * width] = 0.5D - shortest.getDistance() * size / range;
				for (int channel = 0; channel < 3; channel++) {
					final Edge edge = closest[channel].getEdge();
					if (edge != null) {
						edge.pseudoDistance(px, py, closest[channel]);
					}
					field[(x + y * width) * 3 + channel] = 0.5D - closest[channel].getDistance() * size / range;
				}
			}
		}

		Msdf.correct(field, width, height, 1.001D / range);
		Msdf.reconcile(field, truth, 1D / range);

		final int[] pixels = new int[width * height];
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = 255 << 24 | Msdf.clamp(field[i * 3]) << 16 | Msdf.clamp(field[i * 3 + 1]) << 8 | Msdf.clamp(field[i * 3 + 2]);
		}
		return pixels;
	}

	private static void correct(final double[] field, final int width, final int height, final double threshold) {
		final List<Integer> clashes = new ArrayList<>();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final int index = (x + y * width) * 3;
				if (x > 0 && Msdf.clashes(field, index, index - 3, threshold)
						|| x < width - 1 && Msdf.clashes(field, index, index + 3, threshold)
						|| y > 0 && Msdf.clashes(field, index, index - width * 3, threshold)
						|| y < height - 1 && Msdf.clashes(field, index, index + width * 3, threshold)) {
					clashes.add(index);
				}
			}
		}

		for (final int index : clashes) {
			final double median = Msdf.median(field[index], field[index + 1], field[index + 2]);
			field[index] = median;
			field[index + 1] = median;
			field[index + 2] = median;
		}
	}

	private static void reconcile(final double[] field, final double[] truth, final double tolerance) {
		for (int i = 0; i < truth.length; i++) {
			final double value = truth[i];
			if (Math.abs(value - 0.5D) <= tolerance || Msdf.median(field[i * 3], field[i * 3 + 1], field[i * 3 + 2]) >= 0.5D == value >= 0.5D) {
				continue;
			}

			field[i * 3] = value;
			field[i * 3 + 1] = value;
			field[i * 3 + 2] = value;
		}
	}

	private static boolean clashes(final double[] field, final int first, final int second, final double threshold) {
		double a0 = field[first];
		double a1 = field[first + 1];
		double a2 = field[first + 2];
		double b0 = field[second];
		double b1 = field[second + 1];
		double b2 = field[second + 2];

		if (Math.abs(b0 - a0) < Math.abs(b1 - a1)) {
			final double swapA = a0;
			final double swapB = b0;
			a0 = a1;
			a1 = swapA;
			b0 = b1;
			b1 = swapB;
		}

		if (Math.abs(b1 - a1) < Math.abs(b2 - a2)) {
			double swapA = a1;
			double swapB = b1;
			a1 = a2;
			a2 = swapA;
			b1 = b2;
			b2 = swapB;

			if (Math.abs(b0 - a0) < Math.abs(b1 - a1)) {
				swapA = a0;
				swapB = b0;
				a0 = a1;
				a1 = swapA;
				b0 = b1;
				b1 = swapB;
			}
		}

		return Math.abs(b1 - a1) >= threshold && !(b0 == b1 && b0 == b2) && Math.abs(a2 - 0.5D) >= Math.abs(b2 - 0.5D);
	}

	private static double median(final double first, final double second, final double third) {
		return Math.max(Math.min(first, second), Math.min(Math.max(first, second), third));
	}

	private static int clamp(final double value) {
		final int scaled = (int) (value * 255D + 0.5D);
		return scaled < 0 ? 0 : scaled > 255 ? 255 : scaled;
	}

}