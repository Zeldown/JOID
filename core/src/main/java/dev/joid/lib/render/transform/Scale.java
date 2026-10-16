package dev.joid.lib.render.transform;

import java.util.function.Supplier;

import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Scale {

	private Supplier<Double> widthSupplier;
	private Supplier<Double> heightSupplier;
	private Supplier<Double> depthSupplier;

	private Scale(final double width, final double height, final double depth) {
		this.widthSupplier  = () -> width;
		this.heightSupplier = () -> height;
		this.depthSupplier  = () -> depth;
	}

	private Scale(final Supplier<Double> widthSupplier, final Supplier<Double> heightSupplier, final Supplier<Double> depthSupplier) {
		this.widthSupplier  = widthSupplier;
		this.heightSupplier = heightSupplier;
		this.depthSupplier  = depthSupplier;
	}

	public static @NonNull Scale create() {
		return new Scale(1, 1, 1);
	}

	public static @NonNull Scale create(final double width, final double height, final double depth) {
		return new Scale(width, height, depth);
	}

	public static @NonNull Scale create(final @NonNull Supplier<Double> widthSupplier, final @NonNull Supplier<Double> heightSupplier, final @NonNull Supplier<Double> depthSupplier) {
		return new Scale(widthSupplier, heightSupplier, depthSupplier);
	}

	public static @NonNull Scale WIDTH(final double width) {
		return new Scale(width, 1, 1);
	}

	public static @NonNull Scale WIDTH(final @NonNull Supplier<Double> widthSupplier) {
		return new Scale(widthSupplier, () -> 1D, () -> 1D);
	}

	public static @NonNull Scale HEIGHT(final double height) {
		return new Scale(1, height, 1);
	}

	public static @NonNull Scale HEIGHT(final @NonNull Supplier<Double> heightSupplier) {
		return new Scale(() -> 1D, heightSupplier, () -> 1D);
	}

	public static @NonNull Scale DEPTH(final double depth) {
		return new Scale(1, 1, depth);
	}

	public static @NonNull Scale DEPTH(final @NonNull Supplier<Double> depthSupplier) {
		return new Scale(() -> 1D, () -> 1D, depthSupplier);
	}

	public double getWidth() {
		return this.widthSupplier.get();
	}

	public double getHeight() {
		return this.heightSupplier.get();
	}

	public double getDepth() {
		return this.depthSupplier.get();
	}

	public @NonNull Scale width(final double width) {
		this.widthSupplier = () -> width;
		return this;
	}

	public @NonNull Scale width(final @NonNull Supplier<Double> widthSupplier) {
		this.widthSupplier = widthSupplier;
		return this;
	}

	public @NonNull Scale height(final double height) {
		this.heightSupplier = () -> height;
		return this;
	}

	public @NonNull Scale height(final @NonNull Supplier<Double> heightSupplier) {
		this.heightSupplier = heightSupplier;
		return this;
	}

	public @NonNull Scale depth(final double depth) {
		this.depthSupplier = () -> depth;
		return this;
	}

	public @NonNull Scale depth(final @NonNull Supplier<Double> depthSupplier) {
		this.depthSupplier = depthSupplier;
		return this;
	}

}