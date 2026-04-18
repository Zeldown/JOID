package be.zeldown.joid.lib.opengl.modifier;

import lombok.Getter;
import lombok.NonNull;

import java.util.function.Supplier;

@Getter
public class GLRotation {

	public static final GLRotation YAW   = new GLRotation(1, 0, 0);
	public static final GLRotation PITCH = new GLRotation(0, 1, 0);
	public static final GLRotation ROLL  = new GLRotation(0, 0, 1);

	private final Supplier<Double> rawXSupplier;
	private final Supplier<Double> rawYSupplier;
	private final Supplier<Double> rawZSupplier;

	private GLRotation(final double yaw, final double pitch, final double roll) {
		this.rawXSupplier = () -> roll;
		this.rawYSupplier = () -> yaw;
		this.rawZSupplier = () -> pitch;
	}

	private GLRotation(final Supplier<Double> yawSupplier, final Supplier<Double> pitchSupplier, final Supplier<Double> rollSupplier) {
		this.rawXSupplier = rollSupplier;
		this.rawYSupplier = yawSupplier;
		this.rawZSupplier = pitchSupplier;
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

	public static @NonNull GLRotation create() {
	    return new GLRotation(0, 0, 0);
	}

	public static @NonNull GLRotation create(final double yaw, final double pitch, final double roll) {
	    return new GLRotation(yaw, pitch, roll);
	}

	public static @NonNull GLRotation create(final Supplier<Double> yawSupplier, final Supplier<Double> pitchSupplier, final Supplier<Double> rollSupplier) {
	    return new GLRotation(yawSupplier, pitchSupplier, rollSupplier);
	}

}