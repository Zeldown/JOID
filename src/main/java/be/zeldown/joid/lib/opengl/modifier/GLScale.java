package be.zeldown.joid.lib.opengl.modifier;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class GLScale {

	private double rawX;
	private double rawY;
	private double rawZ;

	private GLScale(final double width, final double height, final double depth) {
		this.rawX = width;
		this.rawY = height;
		this.rawZ = depth;
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
	 * Creates a new GLScale instance with the specified width and default height and depth scales (1, 1).
	 *
	 * @param width The scale factor along the x-axis.
	 * @return A new GLScale instance with the specified width scale.
	 */
	public static @NonNull GLScale WIDTH(final double width) {
	    return new GLScale(width, 1, 1);
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
	 * Creates a new GLScale instance with the specified depth and default width and height scales (1, 1).
	 *
	 * @param depth The scale factor along the z-axis.
	 * @return A new GLScale instance with the specified depth scale.
	 */
	public static @NonNull GLScale DEPTH(final double depth) {
	    return new GLScale(1, 1, depth);
	}

	/**
	 * Sets the scale factor along the x-axis of this GLScale instance and returns the modified instance.
	 *
	 * @param width The new scale factor along the x-axis.
	 * @return The modified GLScale instance.
	 */
	public @NonNull GLScale width(final double width) {
	    this.rawX = width;
	    return this;
	}

	/**
	 * Sets the scale factor along the y-axis of this GLScale instance and returns the modified instance.
	 *
	 * @param height The new scale factor along the y-axis.
	 * @return The modified GLScale instance.
	 */
	public @NonNull GLScale height(final double height) {
	    this.rawY = height;
	    return this;
	}

	/**
	 * Sets the scale factor along the z-axis of this GLScale instance and returns the modified instance.
	 *
	 * @param depth The new scale factor along the z-axis.
	 * @return The modified GLScale instance.
	 */
	public @NonNull GLScale depth(final double depth) {
	    this.rawZ = depth;
	    return this;
	}

}