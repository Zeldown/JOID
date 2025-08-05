package be.zeldown.joid.lib.opengl.modifier;

import lombok.Getter;
import lombok.NonNull;

import java.util.function.Supplier;

@Getter
public class GLVector {

	private Supplier<Double> xSupplier;
	private Supplier<Double> ySupplier;
	private Supplier<Double> zSupplier;

	private GLVector(final double x, final double y, final double z) {
		this.xSupplier = () -> x;
		this.ySupplier = () -> y;
		this.zSupplier = () -> z;
	}

	private GLVector(final Supplier<Double> xSupplier, final Supplier<Double> ySupplier, final Supplier<Double> zSupplier) {
		this.xSupplier = xSupplier;
		this.ySupplier = ySupplier;
		this.zSupplier = zSupplier;
	}

	public double getX() {
		return xSupplier.get();
	}

	public double getY() {
		return ySupplier.get();
	}

	public double getZ() {
		return zSupplier.get();
	}

	/**
	 * Creates a new GLCoords instance with default coordinates (0, 0, 0).
	 *
	 * @return A new GLCoords instance with default coordinates.
	 */
	public static @NonNull GLVector create() {
	    return new GLVector(0, 0, 0);
	}

	/**
	 * Creates a new GLCoords instance with the specified coordinates.
	 *
	 * @param x The x-coordinate.
	 * @param y The y-coordinate.
	 * @param z The z-coordinate.
	 * @return A new GLCoords instance with the specified coordinates.
	 */
	public static @NonNull GLVector create(final double x, final double y, final double z) {
	    return new GLVector(x, y, z);
	}

	/**
	 * Creates a new GLCoords instance with the specified coordinate suppliers.
	 *
	 * @param xSupplier The x-coordinate supplier.
	 * @param ySupplier The y-coordinate supplier.
	 * @param zSupplier The z-coordinate supplier.
	 * @return A new GLCoords instance with the specified coordinate suppliers.
	 */
	public static @NonNull GLVector create(final Supplier<Double> xSupplier, final Supplier<Double> ySupplier, final Supplier<Double> zSupplier) {
	    return new GLVector(xSupplier, ySupplier, zSupplier);
	}

	/**
	 * Creates a new GLCoords instance with the specified x and y coordinates, and default z-coordinate (0).
	 *
	 * @param x The x-coordinate.
	 * @param y The y-coordinate.
	 * @return A new GLCoords instance with the specified x and y coordinates.
	 */
	public static @NonNull GLVector create(final double x, final double y) {
	    return new GLVector(x, y, 0);
	}

	/**
	 * Creates a new GLCoords instance with the specified x and y coordinate suppliers, and default z-coordinate (0).
	 *
	 * @param xSupplier The x-coordinate supplier.
	 * @param ySupplier The y-coordinate supplier.
	 * @return A new GLCoords instance with the specified x and y coordinate suppliers.
	 */
	public static @NonNull GLVector create(final Supplier<Double> xSupplier, final Supplier<Double> ySupplier) {
	    return new GLVector(xSupplier, ySupplier, () -> 0.0);
	}

	/**
	 * Creates a new GLCoords instance with the specified x-coordinate and default y and z coordinates (0).
	 *
	 * @param x The x-coordinate.
	 * @return A new GLCoords instance with the specified x-coordinate.
	 */
	public static @NonNull GLVector X(final double x) {
	    return new GLVector(x, 0, 0);
	}

	/**
	 * Creates a new GLCoords instance with the specified x-coordinate supplier and default y and z coordinates (0).
	 *
	 * @param xSupplier The x-coordinate supplier.
	 * @return A new GLCoords instance with the specified x-coordinate supplier.
	 */
	public static @NonNull GLVector X(final Supplier<Double> xSupplier) {
	    return new GLVector(xSupplier, () -> 0.0, () -> 0.0);
	}

	/**
	 * Creates a new GLCoords instance with the specified y-coordinate and default x and z coordinates (0).
	 *
	 * @param y The y-coordinate.
	 * @return A new GLCoords instance with the specified y-coordinate.
	 */
	public static @NonNull GLVector Y(final double y) {
	    return new GLVector(0, y, 0);
	}

	/**
	 * Creates a new GLCoords instance with the specified y-coordinate supplier and default x and z coordinates (0).
	 *
	 * @param ySupplier The y-coordinate supplier.
	 * @return A new GLCoords instance with the specified y-coordinate supplier.
	 */
	public static @NonNull GLVector Y(final Supplier<Double> ySupplier) {
	    return new GLVector(() -> 0.0, ySupplier, () -> 0.0);
	}

	/**
	 * Creates a new GLCoords instance with the specified z-coordinate and default x and y coordinates (0).
	 *
	 * @param z The z-coordinate.
	 * @return A new GLCoords instance with the specified z-coordinate.
	 */
	public static @NonNull GLVector Z(final double z) {
	    return new GLVector(0, 0, z);
	}

	/**
	 * Creates a new GLCoords instance with the specified z-coordinate supplier and default x and y coordinates (0).
	 *
	 * @param zSupplier The z-coordinate supplier.
	 * @return A new GLCoords instance with the specified z-coordinate supplier.
	 */
	public static @NonNull GLVector Z(final Supplier<Double> zSupplier) {
	    return new GLVector(() -> 0.0, () -> 0.0, zSupplier);
	}

	/**
	 * Sets the x-coordinate of this GLCoords instance and returns the modified instance.
	 *
	 * @param x The new x-coordinate.
	 * @return The modified GLCoords instance.
	 */
	public @NonNull GLVector x(final double x) {
	    this.xSupplier = () -> x;
	    return this;
	}

	/**
	 * Sets the x-coordinate supplier of this GLCoords instance and returns the modified instance.
	 *
	 * @param xSupplier The new x-coordinate supplier.
	 * @return The modified GLCoords instance.
	 */
	public @NonNull GLVector x(final Supplier<Double> xSupplier) {
	    this.xSupplier = xSupplier;
	    return this;
	}

	/**
	 * Sets the y-coordinate of this GLCoords instance and returns the modified instance.
	 *
	 * @param y The new y-coordinate.
	 * @return The modified GLCoords instance.
	 */
	public @NonNull GLVector y(final double y) {
	    this.ySupplier = () -> y;
	    return this;
	}

	/**
	 * Sets the y-coordinate supplier of this GLCoords instance and returns the modified instance.
	 *
	 * @param ySupplier The new y-coordinate supplier.
	 * @return The modified GLCoords instance.
	 */
	public @NonNull GLVector y(final Supplier<Double> ySupplier) {
	    this.ySupplier = ySupplier;
	    return this;
	}

	/**
	 * Sets the z-coordinate of this GLCoords instance and returns the modified instance.
	 *
	 * @param z The new z-coordinate.
	 * @return The modified GLCoords instance.
	 */
	public @NonNull GLVector z(final double z) {
	    this.zSupplier = () -> z;
	    return this;
	}

	/**
	 * Sets the z-coordinate supplier of this GLCoords instance and returns the modified instance.
	 *
	 * @param zSupplier The new z-coordinate supplier.
	 * @return The modified GLCoords instance.
	 */
	public @NonNull GLVector z(final Supplier<Double> zSupplier) {
	    this.zSupplier = zSupplier;
	    return this;
	}

	/**
	 * Adds the specified coordinates to this GLCoords instance and returns the modified instance.
	 *
	 * @param x The x-coordinate to add.
	 * @param y The y-coordinate to add.
	 * @param z The z-coordinate to add.
	 * @return The modified GLCoords instance.
	 */
	public @NonNull GLVector add(final double x, final double y, final double z) {
	    final double currentX = this.xSupplier.get();
	    final double currentY = this.ySupplier.get();
	    final double currentZ = this.zSupplier.get();
	    this.xSupplier = () -> currentX + x;
	    this.ySupplier = () -> currentY + y;
	    this.zSupplier = () -> currentZ + z;
	    return this;
	}

	/**
	 * Adds the specified coordinate suppliers to this GLCoords instance and returns the modified instance.
	 *
	 * @param xSupplier The x-coordinate supplier to add.
	 * @param ySupplier The y-coordinate supplier to add.
	 * @param zSupplier The z-coordinate supplier to add.
	 * @return The modified GLCoords instance.
	 */
	public @NonNull GLVector add(final Supplier<Double> xSupplier, final Supplier<Double> ySupplier, final Supplier<Double> zSupplier) {
	    final Supplier<Double> currentXSupplier = this.xSupplier;
	    final Supplier<Double> currentYSupplier = this.ySupplier;
	    final Supplier<Double> currentZSupplier = this.zSupplier;
	    this.xSupplier = () -> currentXSupplier.get() + xSupplier.get();
	    this.ySupplier = () -> currentYSupplier.get() + ySupplier.get();
	    this.zSupplier = () -> currentZSupplier.get() + zSupplier.get();
	    return this;
	}

}