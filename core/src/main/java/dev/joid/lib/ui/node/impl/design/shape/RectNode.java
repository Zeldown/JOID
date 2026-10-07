package dev.joid.lib.ui.node.impl.design.shape;

import java.util.function.Supplier;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.utils.signal.Signal;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class RectNode extends Node {

	private Supplier<Color> color;
	private Supplier<Color> hoveredColor;

	private Supplier<Boolean> borderFill;
	private Supplier<Double>  borderStroke;
	private Supplier<Color>   borderColor;
	private Supplier<Color>   hoveredBorderColor;

	protected RectNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.color = () -> Color.TRANSPARENT;
		this.hoveredColor = () -> null;

		this.borderFill = () -> true;
		this.borderStroke = () -> 0D;
		this.borderColor = () -> Color.TRANSPARENT;
		this.hoveredBorderColor = () -> null;
	}

	public static @NonNull RectNode create(final double x, final double y, final double width, final double height) {
		return new RectNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final Color hoveredColor = this.hoveredColor.get();
		final Color color = hoveredColor != null ? this.color.get().to(hoveredColor, super.hoverValue(1F)) : this.color.get();
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), color);
	}

	public final Color getHoveredColor() {
		return this.hoveredColor.get();
	}

	public final @NonNull Color getColor() {
		return this.color.get();
	}

	public final Color getHoveredBorderColor() {
		return this.hoveredBorderColor.get();
	}

	public final @NonNull Color getBorderColor() {
		return this.borderColor.get();
	}

	public final double getBorderStroke() {
		return this.borderStroke.get();
	}

	public final boolean isBorderFill() {
		return this.borderFill.get();
	}

	public final <T extends RectNode> @NonNull T color(final @NonNull Color color) {
		return this.color(Signal.from(color));
	}

	public final <T extends RectNode> @NonNull T color(final @NonNull Supplier<@NonNull Color> color) {
		this.color = color;
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T hoveredColor(final Color hoveredColor) {
		return this.hoveredColor(Signal.from(hoveredColor));
	}

	public final <T extends RectNode> @NonNull T hoveredColor(final @NonNull Supplier<Color> hoveredColor) {
		this.hoveredColor = hoveredColor;
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T borderColor(final @NonNull Color borderColor) {
		return this.borderColor(Signal.from(borderColor));
	}

	public final <T extends RectNode> @NonNull T borderColor(final @NonNull Supplier<@NonNull Color> borderColor) {
		this.borderColor = borderColor;
		return this.applyBorderEffect();
	}

	public final <T extends RectNode> @NonNull T hoveredBorderColor(final Color hoveredBorderColor) {
		return this.hoveredBorderColor(Signal.from(hoveredBorderColor));
	}

	public final <T extends RectNode> @NonNull T hoveredBorderColor(final @NonNull Supplier<Color> hoveredBorderColor) {
		this.hoveredBorderColor = hoveredBorderColor;
		return this.applyBorderEffect();
	}

	public final <T extends RectNode> @NonNull T borderStroke(final double borderStroke) {
		return this.borderStroke(Signal.from(borderStroke));
	}

	public final <T extends RectNode> @NonNull T borderStroke(final @NonNull Supplier<Double> borderStroke) {
		this.borderStroke = borderStroke;
		return this.applyBorderEffect();
	}

	public final <T extends RectNode> @NonNull T borderFill(final boolean borderFill) {
		return this.borderFill(Signal.from(borderFill));
	}

	public final <T extends RectNode> @NonNull T borderFill(final @NonNull Supplier<Boolean> borderFill) {
		this.borderFill = borderFill;
		return this.applyBorderEffect();
	}

	private <T extends RectNode> T applyBorderEffect() {
		if (!super.hasEffect(BorderNodeEffect.class)) {
			super.effect(BorderNodeEffect.create(Color.TRANSPARENT, 0F).color(this::computeBorderColor).width(() -> this.borderStroke.get().floatValue()).fill(() -> this.borderFill.get()));
		}
		return (T) this;
	}

	private Color computeBorderColor() {
		final Color hovered = this.hoveredBorderColor.get();
		return hovered != null ? this.borderColor.get().to(hovered, super.hoverValue(1F)) : this.borderColor.get();
	}

}