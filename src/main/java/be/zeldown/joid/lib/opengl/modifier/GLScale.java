package be.zeldown.joid.lib.opengl.modifier;

import lombok.Getter;
import lombok.NonNull;

import java.util.function.Supplier;

@Getter
public class GLScale {

	private Supplier<Double> rawXSupplier;
	private Supplier<Double> rawYSupplier;
	private Supplier<Double> rawZSupplier;

	private GLScale(final double width, final double height, final double depth) {
		this.rawXSupplier = () -> width;
		this.rawYSupplier = () -> height;
		this.rawZSupplier = () -> depth;
	}

	private GLScale(final Supplier<Double> widthSupplier, final Supplier<Double> heightSupplier, final Supplier<Double> depthSupplier) {
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

	public static @NonNull GLScale create() {
	    return new GLScale(1, 1, 1);
	}

	public static @NonNull GLScale create(final double width, final double height, final double depth) {
	    return new GLScale(width, height, depth);
	}

	public static @NonNull GLScale create(final Supplier<Double> widthSupplier, final Supplier<Double> heightSupplier, final Supplier<Double> depthSupplier) {
	    return new GLScale(widthSupplier, heightSupplier, depthSupplier);
	}

	public static @NonNull GLScale WIDTH(final double width) {
	    return new GLScale(width, 1, 1);
	}

	public static @NonNull GLScale WIDTH(final Supplier<Double> widthSupplier) {
	    return new GLScale(widthSupplier, () -> 1.0, () -> 1.0);
	}

	public static @NonNull GLScale HEIGHT(final double height) {
	    return new GLScale(1, height, 1);
	}

	public static @NonNull GLScale HEIGHT(final Supplier<Double> heightSupplier) {
	    return new GLScale(() -> 1.0, heightSupplier, () -> 1.0);
	}

	public static @NonNull GLScale DEPTH(final double depth) {
	    return new GLScale(1, 1, depth);
	}

	public static @NonNull GLScale DEPTH(final Supplier<Double> depthSupplier) {
	    return new GLScale(() -> 1.0, () -> 1.0, depthSupplier);
	}

	public @NonNull GLScale width(final double width) {
	    this.rawXSupplier = () -> width;
	    return this;
	}

	public @NonNull GLScale width(final Supplier<Double> widthSupplier) {
	    this.rawXSupplier = widthSupplier;
	    return this;
	}

	public @NonNull GLScale height(final double height) {
	    this.rawYSupplier = () -> height;
	    return this;
	}

	public @NonNull GLScale height(final Supplier<Double> heightSupplier) {
	    this.rawYSupplier = heightSupplier;
	    return this;
	}

	public @NonNull GLScale depth(final double depth) {
	    this.rawZSupplier = () -> depth;
	    return this;
	}

	public @NonNull GLScale depth(final Supplier<Double> depthSupplier) {
	    this.rawZSupplier = depthSupplier;
	    return this;
	}

}