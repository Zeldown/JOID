package dev.joid.lib.render.transform;

import java.util.function.Supplier;

import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Rotation {

	public static final Rotation YAW   = new Rotation(1D, 0D, 0D);
	public static final Rotation ROLL  = new Rotation(0D, 0D, 1D);
	public static final Rotation PITCH = new Rotation(0D, 1D, 0D);

	private final Supplier<Double> yawSupplier;
	private final Supplier<Double> rollSupplier;
	private final Supplier<Double> pitchSupplier;

	private Rotation(final double yaw, final double pitch, final double roll) {
		this(() -> yaw, () -> pitch, () -> roll);
	}

	private Rotation(final Supplier<Double> yawSupplier, final Supplier<Double> pitchSupplier, final Supplier<Double> rollSupplier) {
		this.yawSupplier = yawSupplier;
		this.rollSupplier = rollSupplier;
		this.pitchSupplier = pitchSupplier;
	}

	public static @NonNull Rotation create() {
		return new Rotation(0D, 0D, 0D);
	}

	public static @NonNull Rotation create(final double yaw, final double pitch, final double roll) {
		return new Rotation(yaw, pitch, roll);
	}

	public static @NonNull Rotation create(final @NonNull Supplier<Double> yawSupplier, final @NonNull Supplier<Double> pitchSupplier, final @NonNull Supplier<Double> rollSupplier) {
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