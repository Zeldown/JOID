package be.zeldown.joid.lib.opengl.modifier;

import lombok.Getter;
import lombok.NonNull;

import java.util.function.Supplier;

@Getter
public class GLVector {

	private Supplier<Double> xSupplier;
	private Supplier<Double> ySupplier;
	private Supplier<Double> zSupplier;

	private GLVector(final double x, final double y, final double z) {
		this.xSupplier = () -> x;
		this.ySupplier = () -> y;
		this.zSupplier = () -> z;
	}

	private GLVector(final Supplier<Double> xSupplier, final Supplier<Double> ySupplier, final Supplier<Double> zSupplier) {
		this.xSupplier = xSupplier;
		this.ySupplier = ySupplier;
		this.zSupplier = zSupplier;
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

	public static @NonNull GLVector create() {
	    return new GLVector(0, 0, 0);
	}

	public static @NonNull GLVector create(final double x, final double y, final double z) {
	    return new GLVector(x, y, z);
	}

	public static @NonNull GLVector create(final Supplier<Double> xSupplier, final Supplier<Double> ySupplier, final Supplier<Double> zSupplier) {
	    return new GLVector(xSupplier, ySupplier, zSupplier);
	}

	public static @NonNull GLVector create(final double x, final double y) {
	    return new GLVector(x, y, 0);
	}

	public static @NonNull GLVector create(final Supplier<Double> xSupplier, final Supplier<Double> ySupplier) {
	    return new GLVector(xSupplier, ySupplier, () -> 0.0);
	}

	public static @NonNull GLVector X(final double x) {
	    return new GLVector(x, 0, 0);
	}

	public static @NonNull GLVector X(final Supplier<Double> xSupplier) {
	    return new GLVector(xSupplier, () -> 0.0, () -> 0.0);
	}

	public static @NonNull GLVector Y(final double y) {
	    return new GLVector(0, y, 0);
	}

	public static @NonNull GLVector Y(final Supplier<Double> ySupplier) {
	    return new GLVector(() -> 0.0, ySupplier, () -> 0.0);
	}

	public static @NonNull GLVector Z(final double z) {
	    return new GLVector(0, 0, z);
	}

	public static @NonNull GLVector Z(final Supplier<Double> zSupplier) {
	    return new GLVector(() -> 0.0, () -> 0.0, zSupplier);
	}

	public @NonNull GLVector x(final double x) {
	    this.xSupplier = () -> x;
	    return this;
	}

	public @NonNull GLVector x(final Supplier<Double> xSupplier) {
	    this.xSupplier = xSupplier;
	    return this;
	}

	public @NonNull GLVector y(final double y) {
	    this.ySupplier = () -> y;
	    return this;
	}

	public @NonNull GLVector y(final Supplier<Double> ySupplier) {
	    this.ySupplier = ySupplier;
	    return this;
	}

	public @NonNull GLVector z(final double z) {
	    this.zSupplier = () -> z;
	    return this;
	}

	public @NonNull GLVector z(final Supplier<Double> zSupplier) {
	    this.zSupplier = zSupplier;
	    return this;
	}

	public @NonNull GLVector add(final double x, final double y, final double z) {
	    final double currentX = this.xSupplier.get();
	    final double currentY = this.ySupplier.get();
	    final double currentZ = this.zSupplier.get();
	    this.xSupplier = () -> currentX + x;
	    this.ySupplier = () -> currentY + y;
	    this.zSupplier = () -> currentZ + z;
	    return this;
	}

	public @NonNull GLVector add(final Supplier<Double> xSupplier, final Supplier<Double> ySupplier, final Supplier<Double> zSupplier) {
	    final Supplier<Double> currentXSupplier = this.xSupplier;
	    final Supplier<Double> currentYSupplier = this.ySupplier;
	    final Supplier<Double> currentZSupplier = this.zSupplier;
	    this.xSupplier = () -> currentXSupplier.get() + xSupplier.get();
	    this.ySupplier = () -> currentYSupplier.get() + ySupplier.get();
	    this.zSupplier = () -> currentZSupplier.get() + zSupplier.get();
	    return this;
	}

}