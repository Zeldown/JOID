package dev.joid.lib.ui.node.effect.impl;

import java.util.function.Supplier;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import dev.joid.lib.ui.node.impl.design.shape.CircleNode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class ShadowNodeEffect extends NodeEffect<Node> {

	private Supplier<Float>  blurSupplier;
	private Supplier<Color>  colorSupplier;
	private Supplier<Double> offsetXSupplier;
	private Supplier<Double> offsetYSupplier;

	protected ShadowNodeEffect(final @NonNull Color color, final float blur, final double offsetX, final double offsetY) {
		this.blurSupplier    = () -> blur;
		this.colorSupplier   = () -> color;
		this.offsetXSupplier = () -> offsetX;
		this.offsetYSupplier = () -> offsetY;
	}

	public static @NonNull ShadowNodeEffect create(final @NonNull Color color, final float blur) {
		return new ShadowNodeEffect(color, blur, 0D, 0D);
	}

	public static @NonNull ShadowNodeEffect create(final @NonNull Color color, final float blur, final double offsetX, final double offsetY) {
		return new ShadowNodeEffect(color, blur, offsetX, offsetY);
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
	public void pre(final @NonNull Node node, final double mouseX, final double mouseY) {
		final double x = node.getX() + this.getOffsetX();
		final double y = node.getY() + this.getOffsetY();
		if (node instanceof CircleNode || node.hasEffect(CircleNodeEffect.class)) {
			final double size = Math.min(node.getWidth(), node.getHeight());
			DrawUtils.SHAPE.drawShadow(x + (node.getWidth() - size) / 2D, y + (node.getHeight() - size) / 2D, size, size, this.getColor(), (float) size / 2F, this.getBlur());
			return;
		}

		final RoundedNodeEffect rounded = node.getEffect(RoundedNodeEffect.class);
		final float radius = rounded != null ? rounded.getRadius() : 0F;
		DrawUtils.SHAPE.drawShadow(x, y, node.getWidth(), node.getHeight(), this.getColor(), radius, this.getBlur());
	}

	public final <E extends ShadowNodeEffect> @NonNull E blur(final float blur) {
		this.blurSupplier = () -> blur;
		return (E) this;
	}

	public final <E extends ShadowNodeEffect> @NonNull E blur(final @NonNull Supplier<Float> blurSupplier) {
		this.blurSupplier = blurSupplier;
		return (E) this;
	}

	public final <E extends ShadowNodeEffect> @NonNull E color(final @NonNull Color color) {
		this.colorSupplier = () -> color;
		return (E) this;
	}

	public final <E extends ShadowNodeEffect> @NonNull E color(final @NonNull Supplier<@NonNull Color> colorSupplier) {
		this.colorSupplier = colorSupplier;
		return (E) this;
	}

	public final <E extends ShadowNodeEffect> @NonNull E offset(final double offsetX, final double offsetY) {
		this.offsetXSupplier = () -> offsetX;
		this.offsetYSupplier = () -> offsetY;
		return (E) this;
	}

	public final <E extends ShadowNodeEffect> @NonNull E offset(final @NonNull Supplier<Double> offsetXSupplier, final @NonNull Supplier<Double> offsetYSupplier) {
		this.offsetXSupplier = offsetXSupplier;
		this.offsetYSupplier = offsetYSupplier;
		return (E) this;
	}

}