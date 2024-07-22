package be.zeldown.joid.lib.opengl.modifier;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class GLCoords {

	private double x;
	private double y;
	private double z;

	private GLCoords(final double x, final double y, final double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Creates a new GLCoords instance with default coordinates (0, 0, 0).
	 *
	 * @return A new GLCoords instance with default coordinates.
	 */
	public static @NonNull GLCoords create() {
	    return new GLCoords(0, 0, 0);
	}

	/**
	 * Creates a new GLCoords instance with the specified coordinates.
	 *
	 * @param x The x-coordinate.
	 * @param y The y-coordinate.
	 * @param z The z-coordinate.
	 * @return A new GLCoords instance with the specified coordinates.
	 */
	public static @NonNull GLCoords create(final double x, final double y, final double z) {
	    return new GLCoords(x, y, z);
	}

	/**
	 * Creates a new GLCoords instance with the specified x and y coordinates, and default z-coordinate (0).
	 *
	 * @param x The x-coordinate.
	 * @param y The y-coordinate.
	 * @return A new GLCoords instance with the specified x and y coordinates.
	 */
	public static @NonNull GLCoords create(final double x, final double y) {
	    return new GLCoords(x, y, 0);
	}

	/**
	 * Creates a new GLCoords instance with the specified x-coordinate and default y and z coordinates (0).
	 *
	 * @param x The x-coordinate.
	 * @return A new GLCoords instance with the specified x-coordinate.
	 */
	public static @NonNull GLCoords X(final double x) {
	    return new GLCoords(x, 0, 0);
	}

	/**
	 * Creates a new GLCoords instance with the specified y-coordinate and default x and z coordinates (0).
	 *
	 * @param y The y-coordinate.
	 * @return A new GLCoords instance with the specified y-coordinate.
	 */
	public static @NonNull GLCoords Y(final double y) {
	    return new GLCoords(0, y, 0);
	}

	/**
	 * Creates a new GLCoords instance with the specified z-coordinate and default x and y coordinates (0).
	 *
	 * @param z The z-coordinate.
	 * @return A new GLCoords instance with the specified z-coordinate.
	 */
	public static @NonNull GLCoords Z(final double z) {
	    return new GLCoords(0, 0, z);
	}

	/**
	 * Sets the x-coordinate of this GLCoords instance and returns the modified instance.
	 *
	 * @param x The new x-coordinate.
	 * @return The modified GLCoords instance.
	 */
	public @NonNull GLCoords x(final double x) {
	    this.x = x;
	    return this;
	}

	/**
	 * Sets the y-coordinate of this GLCoords instance and returns the modified instance.
	 *
	 * @param y The new y-coordinate.
	 * @return The modified GLCoords instance.
	 */
	public @NonNull GLCoords y(final double y) {
	    this.y = y;
	    return this;
	}

	/**
	 * Sets the z-coordinate of this GLCoords instance and returns the modified instance.
	 *
	 * @param z The new z-coordinate.
	 * @return The modified GLCoords instance.
	 */
	public @NonNull GLCoords z(final double z) {
	    this.z = z;
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
	public @NonNull GLCoords add(final double x, final double y, final double z) {
	    this.x += x;
	    this.y += y;
	    this.z += z;
	    return this;
	}

}