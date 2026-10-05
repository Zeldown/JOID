package be.zeldown.joid.lib.render.modifier;

import java.util.function.Supplier;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class Vector {

	private Supplier<Double> xSupplier;
	private Supplier<Double> ySupplier;
	private Supplier<Double> zSupplier;

	public static @NonNull Vector create() {
		return new Vector(0, 0, 0);
	}

	public static @NonNull Vector X(final double x) {
		return new Vector(x, 0, 0);
	}

	public static @NonNull Vector Y(final double y) {
		return new Vector(0, y, 0);
	}

	public static @NonNull Vector Z(final double z) {
		return new Vector(0, 0, z);
	}

	private Vector(final double x, final double y, final double z) {
		this.xSupplier = () -> x;
		this.ySupplier = () -> y;
		this.zSupplier = () -> z;
	}

	public static @NonNull Vector X(final Supplier<Double> xSupplier) {
		return new Vector(xSupplier, () -> 0D, () -> 0D);
	}

	public static @NonNull Vector Y(final Supplier<Double> ySupplier) {
		return new Vector(() -> 0D, ySupplier, () -> 0D);
	}

	public static @NonNull Vector Z(final Supplier<Double> zSupplier) {
		return new Vector(() -> 0D, () -> 0D, zSupplier);
	}

	public static @NonNull Vector create(final double x, final double y) {
		return new Vector(x, y, 0);
	}

	public static @NonNull Vector create(final double x, final double y, final double z) {
		return new Vector(x, y, z);
	}

	public static @NonNull Vector create(final Supplier<Double> xSupplier, final Supplier<Double> ySupplier) {
		return new Vector(xSupplier, ySupplier, () -> 0D);
	}

	private Vector(final Supplier<Double> xSupplier, final Supplier<Double> ySupplier, final Supplier<Double> zSupplier) {
		this.xSupplier = xSupplier;
		this.ySupplier = ySupplier;
		this.zSupplier = zSupplier;
	}

	public static @NonNull Vector create(final Supplier<Double> xSupplier, final Supplier<Double> ySupplier, final Supplier<Double> zSupplier) {
		return new Vector(xSupplier, ySupplier, zSupplier);
	}

	public double getX() {
		return xSupplier.get();
	}

	public double getY() {
		return ySupplier.get();
	}

	public double getZ() {
		return zSupplier.get();
	}

	public @NonNull Vector x(final double x) {
		this.xSupplier = () -> x;
		return this;
	}

	public @NonNull Vector y(final double y) {
		this.ySupplier = () -> y;
		return this;
	}

	public @NonNull Vector z(final double z) {
		this.zSupplier = () -> z;
		return this;
	}

	public @NonNull Vector x(final Supplier<Double> xSupplier) {
		this.xSupplier = xSupplier;
		return this;
	}

	public @NonNull Vector y(final Supplier<Double> ySupplier) {
		this.ySupplier = ySupplier;
		return this;
	}

	public @NonNull Vector z(final Supplier<Double> zSupplier) {
		this.zSupplier = zSupplier;
		return this;
	}

	public @NonNull Vector add(final double x, final double y, final double z) {
		final double currentX = this.xSupplier.get();
		final double currentY = this.ySupplier.get();
		final double currentZ = this.zSupplier.get();
		this.xSupplier = () -> currentX + x;
		this.ySupplier = () -> currentY + y;
		this.zSupplier = () -> currentZ + z;
		return this;
	}

	public @NonNull Vector add(final Supplier<Double> xSupplier, final Supplier<Double> ySupplier, final Supplier<Double> zSupplier) {
		final Supplier<Double> currentXSupplier = this.xSupplier;
		final Supplier<Double> currentYSupplier = this.ySupplier;
		final Supplier<Double> currentZSupplier = this.zSupplier;
		this.xSupplier = () -> currentXSupplier.get() + xSupplier.get();
		this.ySupplier = () -> currentYSupplier.get() + ySupplier.get();
		this.zSupplier = () -> currentZSupplier.get() + zSupplier.get();
		return this;
	}

}