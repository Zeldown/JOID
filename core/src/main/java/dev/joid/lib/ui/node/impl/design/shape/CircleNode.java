package dev.joid.lib.ui.node.impl.design.shape;

import java.util.function.Supplier;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import lombok.NonNull;

@SuppressWarnings("unchecked")
public class CircleNode extends Node {

	private Supplier<Color> color;
	private Supplier<Color> hoveredColor;

	protected CircleNode(final double x, final double y, final double diameter) {
		super(x, y, diameter, diameter);

		this.color = () -> Color.WHITE;
		this.hoveredColor = () -> null;
	}

	public static @NonNull CircleNode create(final double x, final double y, final double diameter) {
		return new CircleNode(x, y, diameter);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final Color hoveredColor = this.hoveredColor.get();
		final Color color = hoveredColor != null ? this.color.get().to(hoveredColor, super.hoverValue(1F)) : this.color.get();
		DrawUtils.SHAPE.drawCircle(super.getX() + super.dw(2D), super.getY() + super.dh(2D), color, Math.min(super.getWidth(), super.getHeight()) / 2D);
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawCircle(
				super.getX() + super.dw(2D),
				super.getY() + super.dh(2D),
				Color.LOADING(),
				Math.min(super.getWidth(), super.getHeight()) / 2D
				);
	}

	public final Color getHoveredColor() {
		return this.hoveredColor.get();
	}

	public final @NonNull Color getColor() {
		return this.color.get();
	}

	public final <T extends CircleNode> @NonNull T color(final @NonNull Color color) {
		return this.color(Signal.from(color));
	}

	public final <T extends CircleNode> @NonNull T color(final @NonNull Supplier<@NonNull Color> color) {
		this.color = color;
		return (T) this;
	}

	public final <T extends CircleNode> @NonNull T hoveredColor(final Color hoveredColor) {
		return this.hoveredColor(Signal.from(hoveredColor));
	}

	public final <T extends CircleNode> @NonNull T hoveredColor(final @NonNull Supplier<Color> hoveredColor) {
		this.hoveredColor = hoveredColor;
		return (T) this;
	}

}