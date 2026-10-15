package dev.joid.lib.render.transform;

import java.util.function.Supplier;

import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Scale {

	private Supplier<Double> xSupplier;
	private Supplier<Double> ySupplier;
	private Supplier<Double> zSupplier;

	private Scale(final double width, final double height, final double depth) {
		this.xSupplier = () -> width;
		this.ySupplier = () -> height;
		this.zSupplier = () -> depth;
	}

	private Scale(final Supplier<Double> widthSupplier, final Supplier<Double> heightSupplier, final Supplier<Double> depthSupplier) {
		this.xSupplier = widthSupplier;
		this.ySupplier = heightSupplier;
		this.zSupplier = depthSupplier;
	}

	public static @NonNull Scale create() {
		return new Scale(1, 1, 1);
	}

	public static @NonNull Scale create(final double width, final double height, final double depth) {
		return new Scale(width, height, depth);
	}

	public static @NonNull Scale create(final Supplier<Double> widthSupplier, final Supplier<Double> heightSupplier, final Supplier<Double> depthSupplier) {
		return new Scale(widthSupplier, heightSupplier, depthSupplier);
	}

	public static @NonNull Scale WIDTH(final double width) {
		return new Scale(width, 1, 1);
	}

	public static @NonNull Scale WIDTH(final Supplier<Double> widthSupplier) {
		return new Scale(widthSupplier, () -> 1D, () -> 1D);
	}

	public static @NonNull Scale HEIGHT(final double height) {
		return new Scale(1, height, 1);
	}

	public static @NonNull Scale HEIGHT(final Supplier<Double> heightSupplier) {
		return new Scale(() -> 1D, heightSupplier, () -> 1D);
	}

	public static @NonNull Scale DEPTH(final double depth) {
		return new Scale(1, 1, depth);
	}

	public static @NonNull Scale DEPTH(final Supplier<Double> depthSupplier) {
		return new Scale(() -> 1D, () -> 1D, depthSupplier);
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

	public @NonNull Scale width(final double width) {
		this.xSupplier = () -> width;
		return this;
	}

	public @NonNull Scale width(final Supplier<Double> widthSupplier) {
		this.xSupplier = widthSupplier;
		return this;
	}

	public @NonNull Scale height(final double height) {
		this.ySupplier = () -> height;
		return this;
	}

	public @NonNull Scale height(final Supplier<Double> heightSupplier) {
		this.ySupplier = heightSupplier;
		return this;
	}

	public @NonNull Scale depth(final double depth) {
		this.zSupplier = () -> depth;
		return this;
	}

	public @NonNull Scale depth(final Supplier<Double> depthSupplier) {
		this.zSupplier = depthSupplier;
		return this;
	}

}