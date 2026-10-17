package dev.joid.tool.msdf.geometry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Vector2 {

	private final double x;
	private final double y;

	public static Vector2 of(final double x, final double y) {
		return new Vector2(x, y);
	}

	public Vector2 add(final Vector2 other) {
		return Vector2.of(this.x + other.x, this.y + other.y);
	}

	public Vector2 subtract(final Vector2 other) {
		return Vector2.of(this.x - other.x, this.y - other.y);
	}

	public Vector2 scale(final double factor) {
		return Vector2.of(this.x * factor, this.y * factor);
	}

	public Vector2 normalize() {
		final double length = this.length();
		return length == 0D ? Vector2.of(0D, 0D) : Vector2.of(this.x / length, this.y / length);
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