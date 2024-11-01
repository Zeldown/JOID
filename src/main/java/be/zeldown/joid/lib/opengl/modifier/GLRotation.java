package be.zeldown.joid.lib.opengl.modifier;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class GLRotation {

	public static final GLRotation YAW   = new GLRotation(1, 0, 0);
	public static final GLRotation PITCH = new GLRotation(0, 1, 0);
	public static final GLRotation ROLL  = new GLRotation(0, 0, 1);

	private final double rawX;
	private final double rawY;
	private final double rawZ;

	private GLRotation(final double yaw, final double pitch, final double roll) {
		this.rawX = roll;
		this.rawY = yaw;
		this.rawZ = pitch;
	}

	/**
	 * The function creates a new instance of GLRotation with default values for rotation angles.
	 * 
	 * @return An instance of the GLRotation class with the values (0, 0, 0) is being returned.
	 */
	public static @NonNull GLRotation create() {
	    return new GLRotation(0, 0, 0);
	}

	/**
	 * The function creates a new GLRotation object with the specified yaw, pitch, and roll values.
	 * 
	 * @param yaw The yaw parameter typically represents the rotation around the vertical axis, often
	 * referred to as the yaw axis.
	 * @param pitch The pitch parameter typically represents the rotation around the x-axis in a 3D space.
	 * It is the angle at which an object is tilted up or down.
	 * @param roll The `roll` parameter typically represents the rotation around the z-axis in a 3D space.
	 * It is the rotation about the axis that points directly out of the screen towards you.
	 * @return An instance of the GLRotation class with the specified yaw, pitch, and roll values is being
	 * returned.
	 */
	public static @NonNull GLRotation create(final double yaw, final double pitch, final double roll) {
	    return new GLRotation(yaw, pitch, roll);
	}

}