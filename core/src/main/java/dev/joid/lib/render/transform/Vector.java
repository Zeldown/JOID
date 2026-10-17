package dev.joid.lib.render.transform;

import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Vector {

	private Supplier<Double> xSupplier;
	private Supplier<Double> ySupplier;
	private Supplier<Double> zSupplier;

	private Vector(final double x, final double y, final double z) {
		this(() -> x, () -> y, () -> z);
	}

	public static @NonNull Vector create() {
		return new Vector(0D, 0D, 0D);
	}

	public static @NonNull Vector create(final double x, final double y) {
		return new Vector(x, y, 0D);
	}

	public static @NonNull Vector create(final double x, final double y, final double z) {
		return new Vector(x, y, z);
	}

	public static @NonNull Vector create(final @NonNull Supplier<Double> xSupplier, final @NonNull Supplier<Double> ySupplier) {
		return new Vector(xSupplier, ySupplier, () -> 0D);
	}

	public static @NonNull Vector create(final @NonNull Supplier<Double> xSupplier, final @NonNull Supplier<Double> ySupplier, final @NonNull Supplier<Double> zSupplier) {
		return new Vector(xSupplier, ySupplier, zSupplier);
	}

	public static @NonNull Vector X(final double x) {
		return new Vector(x, 0D, 0D);
	}

	public static @NonNull Vector X(final @NonNull Supplier<Double> xSupplier) {
		return new Vector(xSupplier, () -> 0D, () -> 0D);
	}

	public static @NonNull Vector Y(final double y) {
		return new Vector(0D, y, 0D);
	}

	public static @NonNull Vector Y(final @NonNull Supplier<Double> ySupplier) {
		return new Vector(() -> 0D, ySupplier, () -> 0D);
	}

	public static @NonNull Vector Z(final double z) {
		return new Vector(0D, 0D, z);
	}

	public static @NonNull Vector Z(final @NonNull Supplier<Double> zSupplier) {
		return new Vector(() -> 0D, () -> 0D, zSupplier);
	}

	public double getX() {
		return this.xSupplier.get();
	}

	public double getY() {
		return this.ySupplier.get();
	}

	public double getZ() {
		return this.zSupplier.get();
	}

	public @NonNull Vector x(final double x) {
		this.xSupplier = () -> x;
		return this;
	}

	public @NonNull Vector x(final @NonNull Supplier<Double> xSupplier) {
		this.xSupplier = xSupplier;
		return this;
	}

	public @NonNull Vector y(final double y) {
		this.ySupplier = () -> y;
		return this;
	}

	public @NonNull Vector y(final @NonNull Supplier<Double> ySupplier) {
		this.ySupplier = ySupplier;
		return this;
	}

	public @NonNull Vector z(final double z) {
		this.zSupplier = () -> z;
		return this;
	}

	public @NonNull Vector z(final @NonNull Supplier<Double> zSupplier) {
		this.zSupplier = zSupplier;
		return this;
	}

	public @NonNull Vector add(final double x, final double y, final double z) {
		this.xSupplier = Vector.shift(this.xSupplier, x);
		this.ySupplier = Vector.shift(this.ySupplier, y);
		this.zSupplier = Vector.shift(this.zSupplier, z);
		return this;
	}

	public @NonNull Vector add(final @NonNull Supplier<Double> xSupplier, final @NonNull Supplier<Double> ySupplier, final @NonNull Supplier<Double> zSupplier) {
		final Supplier<Double> currentXSupplier = this.xSupplier;
		final Supplier<Double> currentYSupplier = this.ySupplier;
		final Supplier<Double> currentZSupplier = this.zSupplier;
		this.xSupplier = () -> currentXSupplier.get() + xSupplier.get();
		this.ySupplier = () -> currentYSupplier.get() + ySupplier.get();
		this.zSupplier = () -> currentZSupplier.get() + zSupplier.get();
		return this;
	}

	private static Supplier<Double> shift(final Supplier<Double> supplier, final double offset) {
		if (supplier instanceof Shift) {
			final Shift shift = (Shift) supplier;
			return new Shift(shift.source, shift.offset + offset);
		}

		return new Shift(supplier, offset);
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class Shift implements Supplier<Double> {

		private final Supplier<Double> source;
		private final double           offset;

		@Override
		public Double get() {
			return this.source.get() + this.offset;
		}

	}

}