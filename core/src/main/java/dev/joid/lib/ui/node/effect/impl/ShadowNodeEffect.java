package dev.joid.lib.ui.node.effect.impl;

import java.util.function.Supplier;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import lombok.NonNull;

public class ShadowNodeEffect extends NodeEffect<Node> {

	private Supplier<Float>  blurSupplier;
	private Supplier<Color>  colorSupplier;
	private Supplier<Double> offsetXSupplier;
	private Supplier<Double> offsetYSupplier;

	protected ShadowNodeEffect(final @NonNull Supplier<@NonNull Color> colorSupplier, final @NonNull Supplier<Float> blurSupplier) {
		this.blurSupplier    = blurSupplier;
		this.colorSupplier   = colorSupplier;
		this.offsetXSupplier = () -> 0D;
		this.offsetYSupplier = () -> 0D;
	}

	public static @NonNull ShadowNodeEffect create(final @NonNull Color color, final float blur) {
		return new ShadowNodeEffect(() -> color, () -> blur);
	}

	public static @NonNull ShadowNodeEffect create(final @NonNull Supplier<@NonNull Color> colorSupplier, final @NonNull Supplier<Float> blurSupplier) {
		return new ShadowNodeEffect(colorSupplier, blurSupplier);
	}

	public float getBlur() {
		return this.blurSupplier.get();
	}

	public double getOffsetX() {
		return this.offsetXSupplier.get();
	}

	public double getOffsetY() {
		return this.offsetYSupplier.get();
	}

	public @NonNull Color getColor() {
		return this.colorSupplier.get();
	}

	@Override
	public void draw(final @NonNull Node node, final @NonNull Runnable body) {
		final double x = node.getX() + this.getOffsetX();
		final double y = node.getY() + this.getOffsetY();
		final RoundedNodeEffect rounded = node.getEffect(RoundedNodeEffect.class);
		if (node instanceof CircleNode || node.hasEffect(CircleNodeEffect.class)) {
			final double size = Math.min(node.getWidth(), node.getHeight());
			DrawUtils.SHAPE.drawShadow(x + (node.getWidth() - size) / 2D, y + (node.getHeight() - size) / 2D, size, size, this.getColor(), (float) size / 2F, this.getBlur());
		} else if (rounded == null) {
			DrawUtils.SHAPE.drawShadow(x, y, node.getWidth(), node.getHeight(), this.getColor(), 0F, this.getBlur());
		} else {
			DrawUtils.SHAPE.drawShadow(x, y, node.getWidth(), node.getHeight(), this.getColor(), rounded.getRadius(), rounded.isLeft(), rounded.isTop(), rounded.isRight(), rounded.isBottom(), this.getBlur());
		}
		body.run();
	}

	public final <E extends ShadowNodeEffect> @NonNull E blur(final float blur) {
		return this.blur(Signal.from(blur));
	}

	public final <E extends ShadowNodeEffect> @NonNull E blur(final @NonNull Supplier<Float> blurSupplier) {
		this.blurSupplier = blurSupplier;
		return (E) this;
	}

	public final <E extends ShadowNodeEffect> @NonNull E color(final @NonNull Color color) {
		return this.color(Signal.from(color));
	}

	public final <E extends ShadowNodeEffect> @NonNull E color(final @NonNull Supplier<@NonNull Color> colorSupplier) {
		this.colorSupplier = colorSupplier;
		return (E) this;
	}

	public final <E extends ShadowNodeEffect> @NonNull E offsetX(final double offsetX) {
		return this.offsetX(Signal.from(offsetX));
	}

	public final <E extends ShadowNodeEffect> @NonNull E offsetX(final @NonNull Supplier<Double> offsetXSupplier) {
		this.offsetXSupplier = offsetXSupplier;
		return (E) this;
	}

	public final <E extends ShadowNodeEffect> @NonNull E offsetY(final double offsetY) {
		return this.offsetY(Signal.from(offsetY));
	}

	public final <E extends ShadowNodeEffect> @NonNull E offsetY(final @NonNull Supplier<Double> offsetYSupplier) {
		this.offsetYSupplier = offsetYSupplier;
		return (E) this;
	}

}