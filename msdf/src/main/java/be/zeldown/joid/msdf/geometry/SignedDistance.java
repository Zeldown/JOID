package be.zeldown.joid.msdf.geometry;

import lombok.Getter;
import lombok.Setter;

@Getter
public final class SignedDistance {

	private double distance;
	private double orthogonality;
	private int    side;

	@Setter private Edge edge;

	public SignedDistance() {
		this.reset();
	}

	public void reset() {
		this.distance = -Double.MAX_VALUE;
		this.orthogonality = 1D;
		this.side = 0;
		this.edge = null;
	}

	public void set(final double distance, final double orthogonality, final int side) {
		this.distance = distance;
		this.orthogonality = orthogonality;
		this.side = side;
	}

	public void copy(final SignedDistance other) {
		this.distance = other.distance;
		this.orthogonality = other.orthogonality;
		this.side = other.side;
		this.edge = other.edge;
	}

	public boolean closerThan(final SignedDistance other) {
		final double current = Math.abs(this.distance);
		final double candidate = Math.abs(other.distance);
		return current < candidate || current == candidate && this.orthogonality < other.orthogonality;
	}

}