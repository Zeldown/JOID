package be.zeldown.joid.lib.ui.node.impl.design.shape;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class CircleNode extends Node {

	private Color color;
	private Color hoveredColor;

	protected CircleNode(final double x, final double y, final double diameter) {
		super(x, y, diameter, diameter);
		this.color = Color.WHITE;
	}

	public static @NonNull CircleNode create(final double x, final double y, final double radius) {
		return new CircleNode(x, y, radius);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawCircle(super.getX() + super.dw(2D), super.getY() + super.dw(2D), this.hoveredColor != null ? this.color.to(this.hoveredColor, super.hoverValue(1F)) : this.color, super.dw(2D));
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawCircle(
				super.getX() + super.dw(2D),
				super.getY() + super.dw(2D),
				Color.LOADING(),
				super.dw(2D)
				);
	}

	public final <T extends CircleNode> @NonNull T color(final @NonNull Color color) {
		this.color = color;
		return (T) this;
	}

	public final <T extends CircleNode> @NonNull T color(final @NonNull Color color, final @NonNull Color hoveredColor) {
		this.color = color;
		this.hoveredColor = hoveredColor;
		return (T) this;
	}

	public final <T extends CircleNode> @NonNull T hoveredColor(final @NonNull Color color) {
		this.hoveredColor = color;
		return (T) this;
	}

}