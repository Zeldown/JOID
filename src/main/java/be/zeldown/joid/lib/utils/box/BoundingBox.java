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

    public static BoundingBox create(final double x, final double y, final double width, final double height) {
        return new BoundingBox(x, y, x + width, y + height);
    }

    public BoundingBox copy() {
        return new BoundingBox(this.minX, this.minY, this.maxX, this.maxY);
    }

    public BoundingBox expand(final double value) {
        this.minX -= value;
        this.minY -= value;

        this.maxX += value;
        this.maxY += value;

        return this;
    }

    public BoundingBox contract(final double value) {
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