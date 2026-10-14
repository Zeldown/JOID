package dev.joid.lib.ui.node.effect.impl;

import java.util.function.Supplier;

import dev.joid.lib.render.Drawing;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import lombok.Getter;
import lombok.NonNull;

public class MaskNodeEffect extends NodeEffect<Node> {

	private Supplier<Double>   xSupplier;
	private Supplier<Double>   ySupplier;
	private Supplier<Double>   widthSupplier;
	private Supplier<Double>   heightSupplier;
	private Supplier<Resource> resourceSupplier;

	@Getter private double width;
	@Getter private double height;

	protected MaskNodeEffect() {
		this.xSupplier        = () -> 0D;
		this.ySupplier        = () -> 0D;
		this.resourceSupplier = () -> null;
	}

	public static @NonNull MaskNodeEffect create() {
		return new MaskNodeEffect();
	}

	public double getX() {
		return this.xSupplier.get();
	}

	public double getY() {
		return this.ySupplier.get();
	}

	public Resource getResource() {
		return this.resourceSupplier.get();
	}

	@Override
	public void draw(final @NonNull Node node, final @NonNull Drawing body) {
		final Resource resource = this.resourceSupplier.get();
		final double x = node.getX() + this.xSupplier.get();
		final double y = node.getY() + this.ySupplier.get();
		this.width = this.widthSupplier != null ? this.widthSupplier.get() : node.getWidth();
		this.height = this.heightSupplier != null ? this.heightSupplier.get() : node.getHeight();
		if (resource != null) {
			node.getUi().mask(resource, x, y, this.width, this.height, body);
		} else {
			node.getUi().mask(x, y, this.width, this.height, body);
		}
	}

	public final <E extends MaskNodeEffect> @NonNull E x(final double x) {
		return this.x(Signal.from(x));
	}

	public final <E extends MaskNodeEffect> @NonNull E x(final @NonNull Supplier<Double> xSupplier) {
		this.xSupplier = xSupplier;
		return (E) this;
	}

	public final <E extends MaskNodeEffect> @NonNull E y(final double y) {
		return this.y(Signal.from(y));
	}

	public final <E extends MaskNodeEffect> @NonNull E y(final @NonNull Supplier<Double> ySupplier) {
		this.ySupplier = ySupplier;
		return (E) this;
	}

	public final <E extends MaskNodeEffect> @NonNull E width(final double width) {
		return this.width(Signal.from(width));
	}

	public final <E extends MaskNodeEffect> @NonNull E width(final @NonNull Supplier<Double> widthSupplier) {
		this.widthSupplier = widthSupplier;
		return (E) this;
	}

	public final <E extends MaskNodeEffect> @NonNull E height(final double height) {
		return this.height(Signal.from(height));
	}

	public final <E extends MaskNodeEffect> @NonNull E height(final @NonNull Supplier<Double> heightSupplier) {
		this.heightSupplier = heightSupplier;
		return (E) this;
	}

	public final <E extends MaskNodeEffect> @NonNull E resource(final Resource resource) {
		return this.resource(Signal.from(resource));
	}

	public final <E extends MaskNodeEffect> @NonNull E resource(final @NonNull Supplier<Resource> resourceSupplier) {
		this.resourceSupplier = resourceSupplier;
		return (E) this;
	}

}