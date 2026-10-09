package dev.joid.lib.render.transform;

import java.util.function.Supplier;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class Rotation {

	public static final Rotation YAW   = new Rotation(1, 0, 0);
	public static final Rotation ROLL  = new Rotation(0, 0, 1);
	public static final Rotation PITCH = new Rotation(0, 1, 0);

	private final Supplier<Double> rawXSupplier;
	private final Supplier<Double> rawYSupplier;
	private final Supplier<Double> rawZSupplier;

	private Rotation(final double yaw, final double pitch, final double roll) {
		this.rawXSupplier = () -> pitch;
		this.rawYSupplier = () -> yaw;
		this.rawZSupplier = () -> roll;
	}

	private Rotation(final Supplier<Double> yawSupplier, final Supplier<Double> pitchSupplier, final Supplier<Double> rollSupplier) {
		this.rawXSupplier = pitchSupplier;
		this.rawYSupplier = yawSupplier;
		this.rawZSupplier = rollSupplier;
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

	public double getRawX() {
		return this.rawXSupplier.get();
	}

	public double getRawY() {
		return this.rawYSupplier.get();
	}

	public double getRawZ() {
		return this.rawZSupplier.get();
	}

}