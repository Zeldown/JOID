package dev.joid.tool.msdf.geometry;

import lombok.Getter;

@Getter
public final class Vector2 {

	private final double x;
	private final double y;

	public Vector2(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

	public Vector2 add(final Vector2 other) {
		return new Vector2(this.x + other.x, this.y + other.y);
	}

	public Vector2 subtract(final Vector2 other) {
		return new Vector2(this.x - other.x, this.y - other.y);
	}

	public Vector2 scale(final double factor) {
		return new Vector2(this.x * factor, this.y * factor);
	}

	public Vector2 normalize() {
		final double length = this.length();
		return length == 0D ? new Vector2(0D, 0D) : new Vector2(this.x / length, this.y / length);
	}

	public double dot(final Vector2 other) {
		return this.x * other.x + this.y * other.y;
	}

	public double cross(final Vector2 other) {
		return this.x * other.y - this.y * other.x;
	}

	public double length() {
		return Math.sqrt(this.x * this.x + this.y * this.y);
	}

}