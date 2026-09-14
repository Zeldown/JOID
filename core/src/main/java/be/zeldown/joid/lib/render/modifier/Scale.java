package be.zeldown.joid.lib.render.modifier;

import java.util.function.Supplier;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class Scale {

	private Supplier<Double> rawXSupplier;
	private Supplier<Double> rawYSupplier;
	private Supplier<Double> rawZSupplier;

	private Scale(final double width, final double height, final double depth) {
		this.rawXSupplier = () -> width;
		this.rawYSupplier = () -> height;
		this.rawZSupplier = () -> depth;
	}

	private Scale(final Supplier<Double> widthSupplier, final Supplier<Double> heightSupplier, final Supplier<Double> depthSupplier) {
		this.rawXSupplier = widthSupplier;
		this.rawYSupplier = heightSupplier;
		this.rawZSupplier = depthSupplier;
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
	    return new Scale(widthSupplier, () -> 1.0, () -> 1.0);
	}

	public static @NonNull Scale HEIGHT(final double height) {
	    return new Scale(1, height, 1);
	}

	public static @NonNull Scale HEIGHT(final Supplier<Double> heightSupplier) {
	    return new Scale(() -> 1.0, heightSupplier, () -> 1.0);
	}

	public static @NonNull Scale DEPTH(final double depth) {
	    return new Scale(1, 1, depth);
	}

	public static @NonNull Scale DEPTH(final Supplier<Double> depthSupplier) {
	    return new Scale(() -> 1.0, () -> 1.0, depthSupplier);
	}

	public @NonNull Scale width(final double width) {
	    this.rawXSupplier = () -> width;
	    return this;
	}

	public @NonNull Scale width(final Supplier<Double> widthSupplier) {
	    this.rawXSupplier = widthSupplier;
	    return this;
	}

	public @NonNull Scale height(final double height) {
	    this.rawYSupplier = () -> height;
	    return this;
	}

	public @NonNull Scale height(final Supplier<Double> heightSupplier) {
	    this.rawYSupplier = heightSupplier;
	    return this;
	}

	public @NonNull Scale depth(final double depth) {
	    this.rawZSupplier = () -> depth;
	    return this;
	}

	public @NonNull Scale depth(final Supplier<Double> depthSupplier) {
	    this.rawZSupplier = depthSupplier;
	    return this;
	}

}