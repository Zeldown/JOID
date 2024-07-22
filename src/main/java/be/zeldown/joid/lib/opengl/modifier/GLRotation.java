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
	 * Creates a new GLRotation instance with default rotation angles (0, 0, 0).
	 *
	 * @return A new GLRotation instance with default rotation angles.
	 */
	public static @NonNull GLRotation create() {
	    return new GLRotation(0, 0, 0);
	}

	/**
	 * Creates a new GLRotation instance with the specified yaw, pitch, and roll angles.
	 *
	 * @param yaw   The rotation angle around the vertical axis (yaw).
	 * @param pitch The rotation angle around the lateral axis (pitch).
	 * @param roll  The rotation angle around the longitudinal axis (roll).
	 * @return A new GLRotation instance with the specified rotation angles.
	 */
	public static @NonNull GLRotation create(final double yaw, final double pitch, final double roll) {
	    return new GLRotation(yaw, pitch, roll);
	}

}