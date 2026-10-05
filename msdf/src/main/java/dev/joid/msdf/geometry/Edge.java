package dev.joid.msdf.geometry;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
public final class Edge {

	private final double[] x;
	private final double[] y;
	private final double    total;
	private final double[] lengths;

	@Setter
	private int color;

	public Edge(final List<Vector2> points) {
		this.x = new double[points.size()];
		this.y = new double[points.size()];
		for (int i = 0; i < points.size(); i++) {
			this.x[i] = points.get(i).getX();
			this.y[i] = points.get(i).getY();
		}

		this.lengths = new double[this.x.length - 1];
		double sum = 0D;
		for (int i = 0; i < this.lengths.length; i++) {
			this.lengths[i] = Math.hypot(this.x[i + 1] - this.x[i], this.y[i + 1] - this.y[i]);
			sum += this.lengths[i];
		}
		this.total = sum;
		this.color = 7;
	}

	public Vector2 start() {
		return new Vector2(this.x[0], this.y[0]);
	}

	public Vector2 end() {
		return new Vector2(this.x[this.x.length - 1], this.y[this.y.length - 1]);
	}

	public Vector2 startDirection() {
		return this.directionAt(0, 1);
	}

	public Vector2 endDirection() {
		return this.directionAt(this.x.length - 2, this.x.length - 1);
	}

	public void distance(final double px, final double py, final SignedDistance result) {
		result.reset();

		for (int i = 0; i < this.lengths.length; i++) {
			final double ax = this.x[i];
			final double ay = this.y[i];
			final double bx = this.x[i + 1] - ax;
			final double by = this.y[i + 1] - ay;
			final double square = bx * bx + by * by;
			if (square == 0D) {
				continue;
			}

			final double qx = px - ax;
			final double qy = py - ay;
			double parameter = (qx * bx + qy * by) / square;
			parameter = parameter < 0D ? 0D : parameter > 1D ? 1D : parameter;

			final double dx = parameter <= 0D ? qx : parameter >= 1D ? px - this.x[i + 1] : qx - bx * parameter;
			final double dy = parameter <= 0D ? qy : parameter >= 1D ? py - this.y[i + 1] : qy - by * parameter;
			final double distance = Math.sqrt(dx * dx + dy * dy);
			if (distance > Math.abs(result.getDistance())) {
				continue;
			}

			final double sign = qx * by - qy * bx;
			final double orthogonal = parameter <= 0D || parameter >= 1D ? Math.abs((dx * bx + dy * by) / (Math.sqrt(square) * Math.max(distance, 1E-12D))) : 0D;
			if (distance == Math.abs(result.getDistance()) && orthogonal >= result.getOrthogonality()) {
				continue;
			}

			result.set(sign >= 0D ? distance : -distance, orthogonal, i == 0 && parameter <= 0D ? -1 : i == this.lengths.length - 1 && parameter >= 1D ? 1 : 0);
		}
	}

	public void pseudoDistance(final double px, final double py, final SignedDistance result) {
		if (result.getSide() == 0) {
			return;
		}

		final Vector2 origin = result.getSide() < 0 ? this.start() : this.end();
		final Vector2 direction = result.getSide() < 0 ? this.startDirection().normalize() : this.endDirection().normalize();
		final double qx = px - origin.getX();
		final double qy = py - origin.getY();
		final double along = qx * direction.getX() + qy * direction.getY();
		if (result.getSide() < 0 ? along >= 0D : along <= 0D) {
			return;
		}

		final double pseudo = qx * direction.getY() - qy * direction.getX();
		if (Math.abs(pseudo) <= Math.abs(result.getDistance())) {
			result.set(pseudo, 0D, result.getSide());
		}
	}

	private Vector2 directionAt(final int from, final int to) {
		return new Vector2(this.x[to] - this.x[from], this.y[to] - this.y[from]);
	}

}