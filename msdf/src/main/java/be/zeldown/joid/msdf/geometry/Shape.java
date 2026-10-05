package be.zeldown.joid.msdf.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

@Getter
public final class Shape {

	private final List<List<Edge>> contours = new ArrayList<>();

	public void add(final List<Edge> contour) {
		if (!contour.isEmpty()) {
			this.contours.add(contour);
		}
	}

	public boolean isEmpty() {
		return this.contours.isEmpty();
	}

	public double[] bounds() {
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;

		for (final List<Edge> contour : this.contours) {
			for (final Edge edge : contour) {
				for (int i = 0; i < edge.getX().length; i++) {
					minX = Math.min(minX, edge.getX()[i]);
					minY = Math.min(minY, edge.getY()[i]);
					maxX = Math.max(maxX, edge.getX()[i]);
					maxY = Math.max(maxY, edge.getY()[i]);
				}
			}
		}

		return new double[] {minX, minY, maxX, maxY};
	}

	public void orient() {
		for (int i = 0; i < this.contours.size(); i++) {
			final List<Edge> contour = this.contours.get(i);
			final Vector2 point = contour.get(0).start();

			int depth = 0;
			for (int j = 0; j < this.contours.size(); j++) {
				if (i != j && Shape.contains(this.contours.get(j), point)) {
					depth++;
				}
			}

			if (Shape.area(contour) >= 0D != ((depth & 1) == 0)) {
				this.contours.set(i, Shape.reverse(contour));
			}
		}
	}

	private static boolean contains(final List<Edge> contour, final Vector2 point) {
		boolean inside = false;
		for (final Edge edge : contour) {
			for (int i = 0; i < edge.getX().length - 1; i++) {
				final double ax = edge.getX()[i];
				final double ay = edge.getY()[i];
				final double bx = edge.getX()[i + 1];
				final double by = edge.getY()[i + 1];
				if (ay > point.getY() != by > point.getY() && point.getX() < (bx - ax) * (point.getY() - ay) / (by - ay) + ax) {
					inside = !inside;
				}
			}
		}
		return inside;
	}

	private static List<Edge> reverse(final List<Edge> contour) {
		final List<Edge> reversed = new ArrayList<>();
		for (final Edge edge : contour) {
			final List<Vector2> points = new ArrayList<>();
			for (int i = edge.getX().length - 1; i >= 0; i--) {
				points.add(new Vector2(edge.getX()[i], edge.getY()[i]));
			}

			final Edge flipped = new Edge(points);
			flipped.setColor(edge.getColor());
			reversed.add(flipped);
		}

		Collections.reverse(reversed);
		return reversed;
	}

	private static double area(final List<Edge> contour) {
		double sum = 0D;
		for (final Edge edge : contour) {
			for (int i = 0; i < edge.getX().length - 1; i++) {
				sum += edge.getX()[i] * edge.getY()[i + 1] - edge.getX()[i + 1] * edge.getY()[i];
			}
		}
		return sum / 2D;
	}

}