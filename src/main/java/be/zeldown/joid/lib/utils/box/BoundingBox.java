package be.zeldown.joid.lib.utils.box;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoundingBox {

	private double minX;
	private double minY;
	private double maxX;
	private double maxY;

	private BoundingBox(final double minX, final double minY, final double maxX, final double maxY) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}

	/**
	 * Creates a new BoundingBox instance with the specified position and dimensions.
	 *
	 * @param x      The x-coordinate of the minimum corner.
	 * @param y      The y-coordinate of the minimum corner.
	 * @param width  The width of the bounding box.
	 * @param height The height of the bounding box.
	 * @return A new BoundingBox instance.
	 */
	public static BoundingBox create(final double x, final double y, final double width, final double height) {
		return new BoundingBox(x, y, x + width, y + height);
	}

	/**
	 * Creates a copy of this bounding box.
	 *
	 * @return A new BoundingBox instance with the same coordinates as this instance.
	 */
	public BoundingBox copy() {
		return new BoundingBox(this.minX, this.minY, this.maxX, this.maxY);
	}

	/**
	 * Expands the bounding box by the specified value in all directions.
	 *
	 * @param value The value by which to expand the bounding box.
	 * @return The modified BoundingBox instance.
	 */
	public BoundingBox expand(final double value) {
		this.minX -= value;
		this.minY -= value;

		this.maxX += value;
		this.maxY += value;

		return this;
	}

	/**
	 * Contracts the bounding box by the specified value in all directions.
	 *
	 * @param value The value by which to contract the bounding box.
	 * @return The modified BoundingBox instance.
	 */
	public BoundingBox contract(final double value) {
		this.minX += value;
		this.minY += value;

		this.maxX -= value;
		this.maxY -= value;

		return this;
	}

	/**
	 * Returns the width of the bounding box.
	 * @return
	 */
	public double getWidth() {
		return this.maxX - this.minX;
	}

	/**
	 * Returns the height of the bounding box.
	 * @return
	 */
	public double getHeight() {
		return this.maxY - this.minY;
	}

}