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
	 * The function creates a new instance of GLScale with default scaling values of 1.
	 * 
	 * @return An instance of the GLScale class with values (1.0, 1.0, 1.0) is being returned.
	 */
	public static @NonNull GLScale create() {
		return new GLScale(1D, 1D, 1D);
	}

	/**
	 * The function creates a new GLScale object with the specified width, height, and depth.
	 * 
	 * @param width The width parameter represents the width value for the GLScale object.
	 * @param height height
	 * @param depth The `depth` parameter represents the depth dimension of an object or shape. It is
	 * typically the distance from the front to the back of the object.
	 * @return An instance of the GLScale class with the specified width, height, and depth values is
	 * being returned.
	 */
	public static @NonNull GLScale create(final double width, final double height, final double depth) {
		return new GLScale(width, height, depth);
	}

	/**
	 * The function WIDTH takes a double value representing width and returns a new GLScale object with
	 * the specified width and default height and depth.
	 * 
	 * @param width The `width` parameter is a double value representing the width of a scale.
	 * @return An instance of the GLScale class with the specified width value and default height and
	 * depth values.
	 */
	public static @NonNull GLScale WIDTH(final double width) {
		return new GLScale(width, 1D, 1D);
	}

	/**
	 * The HEIGHT function returns a GLScale object with a specified height value.
	 * 
	 * @param height The `height` parameter is a double value that represents the height component of a
	 * `GLScale` object.
	 * @return A new instance of the GLScale class with the specified height value and default values for
	 * width and depth.
	 */
	public static @NonNull GLScale HEIGHT(final double height) {
		return new GLScale(1D, height, 1D);
	}

	/**
	 * The function DEPTH returns a new GLScale object with specified depth value.
	 * 
	 * @param depth The `DEPTH` method takes a `double` parameter named `depth`, which represents the
	 * depth value used to create a new `GLScale` object with scaling factors of 1 for the x and y axes,
	 * and the specified depth value for the z axis.
	 * @return An instance of the GLScale class with scaling factors of 1 for x and y, and the specified
	 * depth for the z-axis is being returned.
	 */
	public static @NonNull GLScale DEPTH(final double depth) {
		return new GLScale(1D, 1D, depth);
	}

	/**
	 * The `width` function in Java sets the width of a GLScale object and returns the object itself.
	 * 
	 * @param width The `width` parameter is a double value representing the width that will be set to the
	 * `rawX` property of the `GLScale` object.
	 * @return The method is returning the current instance of the GLScale object (`this`) after setting
	 * the `rawX` field to the provided `width` value.
	 */
	public @NonNull GLScale width(final double width) {
		this.rawX = width;
		return this;
	}

	/**
	 * The `height` function in Java sets the raw Y value to the specified height and returns the current
	 * object.
	 * 
	 * @param height The `height` parameter is a double value representing the height that is being set
	 * for a GLScale object.
	 * @return The method is returning the current instance of the `GLScale` object (`this`) after setting
	 * the `rawY` field to the provided `height` value.
	 */
	public @NonNull GLScale height(final double height) {
		this.rawY = height;
		return this;
	}

	/**
	 * The `depth` function in Java sets the depth value and returns the GLScale object.
	 * 
	 * @param depth The `depth` parameter is a double value representing the depth of an object in a 3D
	 * space.
	 * @return The method is returning the current instance of the GLScale object (`this`) after setting
	 * the `rawZ` field to the provided `depth` value.
	 */
	public @NonNull GLScale depth(final double depth) {
		this.rawZ = depth;
		return this;
	}

}