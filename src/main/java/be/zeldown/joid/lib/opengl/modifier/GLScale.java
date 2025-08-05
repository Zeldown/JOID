package be.zeldown.joid.lib.opengl.modifier;

import lombok.Getter;
import lombok.NonNull;

import java.util.function.Supplier;

@Getter
public class GLScale {

	private Supplier<Double> rawXSupplier;
	private Supplier<Double> rawYSupplier;
	private Supplier<Double> rawZSupplier;

	private GLScale(final double width, final double height, final double depth) {
		this.rawXSupplier = () -> width;
		this.rawYSupplier = () -> height;
		this.rawZSupplier = () -> depth;
	}

	private GLScale(final Supplier<Double> widthSupplier, final Supplier<Double> heightSupplier, final Supplier<Double> depthSupplier) {
		this.rawXSupplier = widthSupplier;
		this.rawYSupplier = heightSupplier;
		this.rawZSupplier = depthSupplier;
	}

	public double getRawX() {
		return rawXSupplier.get();
	}

	public double getRawY() {
		return rawYSupplier.get();
	}

	public double getRawZ() {
		return rawZSupplier.get();
	}

	/**
	 * Creates a new GLScale instance with default scale factors (1, 1, 1).
	 *
	 * @return A new GLScale instance with default scale factors.
	 */
	public static @NonNull GLScale create() {
	    return new GLScale(1, 1, 1);
	}

	/**
	 * Creates a new GLScale instance with the specified scale factors.
	 *
	 * @param width  The scale factor along the x-axis.
	 * @param height The scale factor along the y-axis.
	 * @param depth  The scale factor along the z-axis.
	 * @return A new GLScale instance with the specified scale factors.
	 */
	public static @NonNull GLScale create(final double width, final double height, final double depth) {
	    return new GLScale(width, height, depth);
	}

	/**
	 * Creates a new GLScale instance with the specified scale factor suppliers.
	 *
	 * @param widthSupplier  The scale factor supplier along the x-axis.
	 * @param heightSupplier The scale factor supplier along the y-axis.
	 * @param depthSupplier  The scale factor supplier along the z-axis.
	 * @return A new GLScale instance with the specified scale factor suppliers.
	 */
	public static @NonNull GLScale create(final Supplier<Double> widthSupplier, final Supplier<Double> heightSupplier, final Supplier<Double> depthSupplier) {
	    return new GLScale(widthSupplier, heightSupplier, depthSupplier);
	}

	/**
	 * Creates a new GLScale instance with the specified width and default height and depth scales (1, 1).
	 *
	 * @param width The scale factor along the x-axis.
	 * @return A new GLScale instance with the specified width scale.
	 */
	public static @NonNull GLScale WIDTH(final double width) {
	    return new GLScale(width, 1, 1);
	}

	/**
	 * Creates a new GLScale instance with the specified width supplier and default height and depth scales (1, 1).
	 *
	 * @param widthSupplier The scale factor supplier along the x-axis.
	 * @return A new GLScale instance with the specified width scale supplier.
	 */
	public static @NonNull GLScale WIDTH(final Supplier<Double> widthSupplier) {
	    return new GLScale(widthSupplier, () -> 1.0, () -> 1.0);
	}

	/**
	 * Creates a new GLScale instance with the specified height and default width and depth scales (1, 1).
	 *
	 * @param height The scale factor along the y-axis.
	 * @return A new GLScale instance with the specified height scale.
	 */
	public static @NonNull GLScale HEIGHT(final double height) {
	    return new GLScale(1, height, 1);
	}

	/**
	 * Creates a new GLScale instance with the specified height supplier and default width and depth scales (1, 1).
	 *
	 * @param heightSupplier The scale factor supplier along the y-axis.
	 * @return A new GLScale instance with the specified height scale supplier.
	 */
	public static @NonNull GLScale HEIGHT(final Supplier<Double> heightSupplier) {
	    return new GLScale(() -> 1.0, heightSupplier, () -> 1.0);
	}

	/**
	 * Creates a new GLScale instance with the specified depth and default width and height scales (1, 1).
	 *
	 * @param depth The scale factor along the z-axis.
	 * @return A new GLScale instance with the specified depth scale.
	 */
	public static @NonNull GLScale DEPTH(final double depth) {
	    return new GLScale(1, 1, depth);
	}

	/**
	 * Creates a new GLScale instance with the specified depth supplier and default width and height scales (1, 1).
	 *
	 * @param depthSupplier The scale factor supplier along the z-axis.
	 * @return A new GLScale instance with the specified depth scale supplier.
	 */
	public static @NonNull GLScale DEPTH(final Supplier<Double> depthSupplier) {
	    return new GLScale(() -> 1.0, () -> 1.0, depthSupplier);
	}

	/**
	 * Sets the scale factor along the x-axis of this GLScale instance and returns the modified instance.
	 *
	 * @param width The new scale factor along the x-axis.
	 * @return The modified GLScale instance.
	 */
	public @NonNull GLScale width(final double width) {
	    this.rawXSupplier = () -> width;
	    return this;
	}

	/**
	 * Sets the scale factor supplier along the x-axis of this GLScale instance and returns the modified instance.
	 *
	 * @param widthSupplier The new scale factor supplier along the x-axis.
	 * @return The modified GLScale instance.
	 */
	public @NonNull GLScale width(final Supplier<Double> widthSupplier) {
	    this.rawXSupplier = widthSupplier;
	    return this;
	}

	/**
	 * Sets the scale factor along the y-axis of this GLScale instance and returns the modified instance.
	 *
	 * @param height The new scale factor along the y-axis.
	 * @return The modified GLScale instance.
	 */
	public @NonNull GLScale height(final double height) {
	    this.rawYSupplier = () -> height;
	    return this;
	}

	/**
	 * Sets the scale factor supplier along the y-axis of this GLScale instance and returns the modified instance.
	 *
	 * @param heightSupplier The new scale factor supplier along the y-axis.
	 * @return The modified GLScale instance.
	 */
	public @NonNull GLScale height(final Supplier<Double> heightSupplier) {
	    this.rawYSupplier = heightSupplier;
	    return this;
	}

	/**
	 * Sets the scale factor along the z-axis of this GLScale instance and returns the modified instance.
	 *
	 * @param depth The new scale factor along the z-axis.
	 * @return The modified GLScale instance.
	 */
	public @NonNull GLScale depth(final double depth) {
	    this.rawZSupplier = () -> depth;
	    return this;
	}

	/**
	 * Sets the scale factor supplier along the z-axis of this GLScale instance and returns the modified instance.
	 *
	 * @param depthSupplier The new scale factor supplier along the z-axis.
	 * @return The modified GLScale instance.
	 */
	public @NonNull GLScale depth(final Supplier<Double> depthSupplier) {
	    this.rawZSupplier = depthSupplier;
	    return this;
	}

}