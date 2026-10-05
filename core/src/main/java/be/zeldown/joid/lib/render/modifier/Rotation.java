package be.zeldown.joid.lib.render.modifier;

import java.util.function.Supplier;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class Rotation {

	public static final Rotation YAW   = new Rotation(1, 0, 0);
	public static final Rotation PITCH = new Rotation(0, 1, 0);
	public static final Rotation ROLL  = new Rotation(0, 0, 1);

	private final Supplier<Double> rawXSupplier;
	private final Supplier<Double> rawYSupplier;
	private final Supplier<Double> rawZSupplier;

	private Rotation(final double yaw, final double pitch, final double roll) {
		this.rawXSupplier = () -> roll;
		this.rawYSupplier = () -> yaw;
		this.rawZSupplier = () -> pitch;
	}

	private Rotation(final Supplier<Double> yawSupplier, final Supplier<Double> pitchSupplier, final Supplier<Double> rollSupplier) {
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

	public static @NonNull Rotation create() {
		return new Rotation(0, 0, 0);
	}

	public static @NonNull Rotation create(final double yaw, final double pitch, final double roll) {
		return new Rotation(yaw, pitch, roll);
	}

	public static @NonNull Rotation create(final Supplier<Double> yawSupplier, final Supplier<Double> pitchSupplier, final Supplier<Double> rollSupplier) {
		return new Rotation(yawSupplier, pitchSupplier, rollSupplier);
	}

}