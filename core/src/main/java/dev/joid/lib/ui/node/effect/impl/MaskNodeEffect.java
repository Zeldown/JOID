package dev.joid.lib.ui.node.effect.impl;

import java.util.function.Supplier;

import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import dev.joid.lib.utils.signal.Signal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class MaskNodeEffect extends NodeEffect<Node> {

	private Supplier<Resource> resourceSupplier;
	private Supplier<Double> xSupplier;
	private Supplier<Double> ySupplier;
	private Supplier<Double> widthSupplier;
	private Supplier<Double> heightSupplier;

	protected MaskNodeEffect(final double x, final double y, final double width, final double height) {
		this.resourceSupplier = () -> null;
		this.xSupplier = () -> x;
		this.ySupplier = () -> y;
		this.widthSupplier = () -> width;
		this.heightSupplier = () -> height;
	}

	public static @NonNull MaskNodeEffect create(final @NonNull Node node) {
		final MaskNodeEffect effect = new MaskNodeEffect(0, 0, 0, 0);
		effect.xSupplier = () -> 0D;
		effect.ySupplier = () -> 0D;
		effect.widthSupplier = () -> node.w();
		effect.heightSupplier = () -> node.h();
		return effect;
	}

	public static @NonNull MaskNodeEffect create(final double width, final double height) {
		return new MaskNodeEffect(0, 0, width, height);
	}

	public static @NonNull MaskNodeEffect create(final @NonNull Resource resource, final @NonNull Node node) {
		final MaskNodeEffect effect = MaskNodeEffect.create(node);
		effect.resourceSupplier = () -> resource;
		return effect;
	}

	public static @NonNull MaskNodeEffect create(final double x, final double y, final double width, final double height) {
		return new MaskNodeEffect(x, y, width, height);
	}

	public static @NonNull MaskNodeEffect create(final @NonNull Resource resource, final double width, final double height) {
		return MaskNodeEffect.create(resource, 0D, 0D, width, height);
	}

	public static @NonNull MaskNodeEffect create(final @NonNull Supplier<Double> widthSupplier, final @NonNull Supplier<Double> heightSupplier) {
		return MaskNodeEffect.create(() -> 0D, () -> 0D, widthSupplier, heightSupplier);
	}

	public static @NonNull MaskNodeEffect create(final @NonNull Resource resource, final double x, final double y, final double width, final double height) {
		final MaskNodeEffect effect = new MaskNodeEffect(x, y, width, height);
		effect.resourceSupplier = () -> resource;
		return effect;
	}

	public static @NonNull MaskNodeEffect create(final @NonNull Supplier<Double> xSupplier, final @NonNull Supplier<Double> ySupplier, final @NonNull Supplier<Double> widthSupplier, final @NonNull Supplier<Double> heightSupplier) {
		final MaskNodeEffect effect = new MaskNodeEffect(0, 0, 0, 0);
		effect.xSupplier = xSupplier;
		effect.ySupplier = ySupplier;
		effect.widthSupplier = widthSupplier;
		effect.heightSupplier = heightSupplier;
		return effect;
	}

	public Resource getResource() {
		return this.resourceSupplier.get();
	}

	public double getX() {
		return this.xSupplier.get();
	}

	public double getY() {
		return this.ySupplier.get();
	}

	public double getWidth() {
		return this.widthSupplier.get();
	}

	public double getHeight() {
		return this.heightSupplier.get();
	}

	@Override
	public void pre(final @NonNull Node node, final double mouseX, final double mouseY) {
		final Resource resource = this.getResource();
		if (resource != null) {
			node.getUi().startMask(resource, node.getX() + this.getX(), node.getY() + this.getY(), this.getWidth(), this.getHeight());
		} else {
			node.getUi().startMask(node.getX() + this.getX(), node.getY() + this.getY(), this.getWidth(), this.getHeight());
		}
	}

	@Override
	public void post(final @NonNull Node node, final double mouseX, final double mouseY) {
		node.getUi().stopMask();
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