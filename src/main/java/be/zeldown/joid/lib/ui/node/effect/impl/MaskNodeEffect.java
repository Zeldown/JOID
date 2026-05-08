package be.zeldown.joid.lib.ui.node.effect.impl;

import java.util.function.Supplier;

import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.effect.NodeEffect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MaskNodeEffect<T extends Node> extends NodeEffect<T> {

	private Resource resource;
	private Supplier<Double> xSupplier;
	private Supplier<Double> ySupplier;
	private Supplier<Double> widthSupplier;
	private Supplier<Double> heightSupplier;

	private MaskNodeEffect(final double x, final double y, final double width, final double height) {
		this.xSupplier = () -> x;
		this.ySupplier = () -> y;
		this.widthSupplier = () -> width;
		this.heightSupplier = () -> height;
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

	public static <T extends Node> @NonNull MaskNodeEffect<T> create(final double width, final double height) {
		return new MaskNodeEffect<>(0, 0, width, height);
	}

	public static <T extends Node> @NonNull MaskNodeEffect<T> create(final double x, final double y, final double width, final double height) {
		return new MaskNodeEffect<>(x, y, width, height);
	}

	public static <T extends Node> @NonNull MaskNodeEffect<T> create(final @NonNull Node node) {
		final MaskNodeEffect<T> effect = new MaskNodeEffect<>(0, 0, 0, 0);
		effect.xSupplier = () -> 0D;
		effect.ySupplier = () -> 0D;
		effect.widthSupplier = () -> node.w();
		effect.heightSupplier = () -> node.h();
		return effect;
	}

	public static <T extends Node> @NonNull MaskNodeEffect<T> create(final @NonNull Supplier<Double> xSupplier, final @NonNull Supplier<Double> ySupplier, final @NonNull Supplier<Double> widthSupplier, final @NonNull Supplier<Double> heightSupplier) {
		final MaskNodeEffect<T> effect = new MaskNodeEffect<>(0, 0, 0, 0);
		effect.xSupplier = xSupplier;
		effect.ySupplier = ySupplier;
		effect.widthSupplier = widthSupplier;
		effect.heightSupplier = heightSupplier;
		return effect;
	}

	public static <T extends Node> @NonNull MaskNodeEffect<T> create(final @NonNull Supplier<Double> widthSupplier, final @NonNull Supplier<Double> heightSupplier) {
		return MaskNodeEffect.create(() -> 0.0, () -> 0.0, widthSupplier, heightSupplier);
	}

	public static <T extends Node> @NonNull MaskNodeEffect<T> create(final @NonNull Resource resource, final double width, final double height) {
		return MaskNodeEffect.create(resource, 0D, 0D, width, height);
	}

	public static <T extends Node> @NonNull MaskNodeEffect<T> create(final @NonNull Resource resource, final double x, final double y, final double width, final double height) {
		final MaskNodeEffect<T> effect = new MaskNodeEffect<>(x, y, width, height);
		effect.resource = resource;
		return effect;
	}

	public static <T extends Node> @NonNull MaskNodeEffect<T> create(final @NonNull Resource resource, final @NonNull Node node) {
		final MaskNodeEffect<T> effect = MaskNodeEffect.create(node);
		effect.resource = resource;
		return effect;
	}

	/* [ Internal Section ] */
	@Override
	public void pre(final @NonNull T node, final double mouseX, final double mouseY) {
		if (this.resource != null) {
			node.getUi().startMask(this.resource, node.getX() + this.getX(), node.getY() + this.getY(), this.getWidth(), this.getHeight());
		} else {
			node.getUi().startMask(node.getX() + this.getX(), node.getY() + this.getY(), this.getWidth(), this.getHeight());
		}
	}

	@Override
	public void post(final @NonNull T node, final double mouseX, final double mouseY) {
		node.getUi().stopMask();
	}

	/* [ Setter Section ] */
	public <E extends MaskNodeEffect<T>> @NonNull E x(final double x) {
		this.xSupplier = () -> x;
		return (E) this;
	}

	public <E extends MaskNodeEffect<T>> @NonNull E x(final @NonNull Supplier<Double> xSupplier) {
		this.xSupplier = xSupplier;
		return (E) this;
	}

	public <E extends MaskNodeEffect<T>> @NonNull E y(final double y) {
		this.ySupplier = () -> y;
		return (E) this;
	}

	public <E extends MaskNodeEffect<T>> @NonNull E y(final @NonNull Supplier<Double> ySupplier) {
		this.ySupplier = ySupplier;
		return (E) this;
	}

	public <E extends MaskNodeEffect<T>> @NonNull E width(final double width) {
		this.widthSupplier = () -> width;
		return (E) this;
	}

	public <E extends MaskNodeEffect<T>> @NonNull E width(final @NonNull Supplier<Double> widthSupplier) {
		this.widthSupplier = widthSupplier;
		return (E) this;
	}

	public <E extends MaskNodeEffect<T>> @NonNull E height(final double height) {
		this.heightSupplier = () -> height;
		return (E) this;
	}

	public <E extends MaskNodeEffect<T>> @NonNull E height(final @NonNull Supplier<Double> heightSupplier) {
		this.heightSupplier = heightSupplier;
		return (E) this;
	}

	public <E extends MaskNodeEffect<T>> @NonNull E position(final double x, final double y) {
		this.xSupplier = () -> x;
		this.ySupplier = () -> y;
		return (E) this;
	}

	public <E extends MaskNodeEffect<T>> @NonNull E position(final @NonNull Supplier<Double> xSupplier, final @NonNull Supplier<Double> ySupplier) {
		this.xSupplier = xSupplier;
		this.ySupplier = ySupplier;
		return (E) this;
	}

	public <E extends MaskNodeEffect<T>> @NonNull E size(final double width, final double height) {
		this.widthSupplier = () -> width;
		this.heightSupplier = () -> height;
		return (E) this;
	}

	public <E extends MaskNodeEffect<T>> @NonNull E size(final @NonNull Supplier<Double> widthSupplier, final @NonNull Supplier<Double> heightSupplier) {
		this.widthSupplier = widthSupplier;
		this.heightSupplier = heightSupplier;
		return (E) this;
	}

	public <E extends MaskNodeEffect<T>> @NonNull E bounds(final double x, final double y, final double width, final double height) {
		this.xSupplier = () -> x;
		this.ySupplier = () -> y;
		this.widthSupplier = () -> width;
		this.heightSupplier = () -> height;
		return (E) this;
	}

	public <E extends MaskNodeEffect<T>> @NonNull E bounds(final @NonNull Supplier<Double> xSupplier, final @NonNull Supplier<Double> ySupplier, final @NonNull Supplier<Double> widthSupplier, final @NonNull Supplier<Double> heightSupplier) {
		this.xSupplier = xSupplier;
		this.ySupplier = ySupplier;
		this.widthSupplier = widthSupplier;
		this.heightSupplier = heightSupplier;
		return (E) this;
	}

	public <E extends MaskNodeEffect<T>> @NonNull E resource(final Resource resource) {
		this.resource = resource;
		return (E) this;
	}

}