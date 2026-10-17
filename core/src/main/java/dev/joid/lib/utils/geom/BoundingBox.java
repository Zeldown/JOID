package dev.joid.lib.utils.geom;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class BoundingBox {

	private double minX;
	private double minY;
	private double maxX;
	private double maxY;

	public static @NonNull BoundingBox create(final double x, final double y, final double width, final double height) {
		return new BoundingBox(x, y, x + width, y + height);
	}

	public @NonNull BoundingBox copy() {
		return new BoundingBox(this.minX, this.minY, this.maxX, this.maxY);
	}

	public @NonNull BoundingBox expand(final double value) {
		this.minX -= value;
		this.minY -= value;

		this.maxX += value;
		this.maxY += value;

		return this;
	}

	public @NonNull BoundingBox contract(final double value) {
		this.minX += value;
		this.minY += value;

		this.maxX -= value;
		this.maxY -= value;

		return this;
	}

	public double getWidth() {
		return this.maxX - this.minX;
	}

	public double getHeight() {
		return this.maxY - this.minY;
	}

}