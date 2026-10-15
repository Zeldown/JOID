package dev.joid.lib.render.transform;

import java.util.function.Supplier;

import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Rotation {

	public static final Rotation YAW   = new Rotation(1, 0, 0);
	public static final Rotation ROLL  = new Rotation(0, 0, 1);
	public static final Rotation PITCH = new Rotation(0, 1, 0);

	private final Supplier<Double> yawSupplier;
	private final Supplier<Double> rollSupplier;
	private final Supplier<Double> pitchSupplier;

	private Rotation(final double yaw, final double pitch, final double roll) {
		this.yawSupplier = () -> yaw;
		this.rollSupplier = () -> roll;
		this.pitchSupplier = () -> pitch;
	}

	private Rotation(final Supplier<Double> yawSupplier, final Supplier<Double> pitchSupplier, final Supplier<Double> rollSupplier) {
		this.yawSupplier = yawSupplier;
		this.rollSupplier = rollSupplier;
		this.pitchSupplier = pitchSupplier;
	}

	public static @NonNull Rotation create() {
		return new Rotation(0, 0, 0);
	}

	public static @NonNull Rotation create(final double yaw, final double pitch, final double roll) {
		return new Rotation(yaw, pitch, roll);
	}

	public static @NonNull Rotation create(final Supplier<Double> yawSupplier, final Supplier<Double> pitchSupplier, final Supplier<Double> rollSupplier) {
		return new Rotation(yawSupplier, pitchSupplier, rollSupplier);
	}

	public double getYaw() {
		return this.yawSupplier.get();
	}

	public double getRoll() {
		return this.rollSupplier.get();
	}

	public double getPitch() {
		return this.pitchSupplier.get();
	}

}