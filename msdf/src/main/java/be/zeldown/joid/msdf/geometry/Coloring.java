package be.zeldown.joid.msdf.geometry;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Coloring {

	private static final int[] COLORS = {3, 5, 6};

	public static void apply(final Shape shape, final double threshold) {
		final double sine = Math.sin(threshold);
		for (final List<Edge> contour : shape.getContours()) {
			Coloring.apply(contour, sine);
		}
	}

	private static void apply(final List<Edge> contour, final double sine) {
		final List<Integer> corners = new ArrayList<>();
		for (int i = 0; i < contour.size(); i++) {
			final Edge previous = contour.get((i + contour.size() - 1) % contour.size());
			if (Coloring.isCorner(previous.endDirection(), contour.get(i).startDirection(), sine)) {
				corners.add(i);
			}
		}

		if (corners.isEmpty()) {
			for (final Edge edge : contour) {
				edge.setColor(7);
			}
			return;
		}

		if (corners.size() == 1) {
			final int corner = corners.get(0);
			for (int i = 0; i < contour.size(); i++) {
				contour.get((corner + i) % contour.size()).setColor(Coloring.COLORS[i * 3 / contour.size() % 3]);
			}
			return;
		}

		int spline = 0;
		final int start = corners.get(0);
		for (int i = 0; i < contour.size(); i++) {
			final int index = (start + i) % contour.size();
			if (spline + 1 < corners.size() && corners.get(spline + 1) == index) {
				spline++;
			}
			contour.get(index).setColor(Coloring.color(spline, corners.size()));
		}
	}

	private static int color(final int spline, final int splines) {
		if (spline == splines - 1 && splines % 3 == 1) {
			return Coloring.COLORS[1];
		}
		return Coloring.COLORS[spline % 3];
	}

	private static boolean isCorner(final Vector2 incoming, final Vector2 outgoing, final double sine) {
		final Vector2 from = incoming.normalize();
		final Vector2 to = outgoing.normalize();
		return from.dot(to) <= 0D || Math.abs(from.cross(to)) > sine;
	}

}