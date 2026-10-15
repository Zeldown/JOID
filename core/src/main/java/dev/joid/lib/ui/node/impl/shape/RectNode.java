package dev.joid.lib.ui.node.impl.shape;

import java.util.function.Supplier;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class RectNode extends Node {

	private Color color;
	private Color hoveredColor;

	private Color borderColor;
	private Color hoveredBorderColor;

	protected RectNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.color       = Color.TRANSPARENT;
		this.borderColor = Color.TRANSPARENT;
	}

	public static @NonNull RectNode create(final double x, final double y, final double width, final double height) {
		return new RectNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), super.hoverColor(this.color, this.hoveredColor));
	}

	public final double getBorderStroke() {
		final BorderNodeEffect border = super.getEffect(BorderNodeEffect.class);
		return border == null ? 0D : border.getWidth();
	}

	public final boolean isBorderFill() {
		final BorderNodeEffect border = super.getEffect(BorderNodeEffect.class);
		return border == null || border.isFill();
	}

	public final <T extends RectNode> @NonNull T color(final @NonNull Color color) {
		return this.color(Signal.from(color));
	}

	public final <T extends RectNode> @NonNull T color(final @NonNull Supplier<@NonNull Color> color) {
		return super.follow("color", color, value -> this.color = value);
	}

	public final <T extends RectNode> @NonNull T hoveredColor(final Color hoveredColor) {
		return this.hoveredColor(Signal.from(hoveredColor));
	}

	public final <T extends RectNode> @NonNull T hoveredColor(final @NonNull Supplier<Color> hoveredColor) {
		return super.follow("hoveredColor", hoveredColor, value -> this.hoveredColor = value);
	}

	public final <T extends RectNode> @NonNull T borderColor(final @NonNull Color borderColor) {
		return this.borderColor(Signal.from(borderColor));
	}

	public final <T extends RectNode> @NonNull T borderColor(final @NonNull Supplier<@NonNull Color> borderColor) {
		super.follow("borderColor", borderColor, value -> this.borderColor = value);
		this.getBorder().color(RectNode::computeBorderColor);
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T hoveredBorderColor(final Color hoveredBorderColor) {
		return this.hoveredBorderColor(Signal.from(hoveredBorderColor));
	}

	public final <T extends RectNode> @NonNull T hoveredBorderColor(final @NonNull Supplier<Color> hoveredBorderColor) {
		super.follow("hoveredBorderColor", hoveredBorderColor, value -> this.hoveredBorderColor = value);
		this.getBorder().color(RectNode::computeBorderColor);
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T borderStroke(final double borderStroke) {
		return this.borderStroke(Signal.from(borderStroke));
	}

	public final <T extends RectNode> @NonNull T borderStroke(final @NonNull Supplier<Double> borderStroke) {
		this.getBorder().width(() -> borderStroke.get().floatValue());
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T borderFill(final boolean borderFill) {
		return this.borderFill(Signal.from(borderFill));
	}

	public final <T extends RectNode> @NonNull T borderFill(final @NonNull Supplier<Boolean> borderFill) {
		this.getBorder().fill(borderFill);
		return (T) this;
	}

	private @NonNull BorderNodeEffect getBorder() {
		final BorderNodeEffect border = super.getEffect(BorderNodeEffect.class);
		if (border != null) {
			return border;
		}

		final BorderNodeEffect created = BorderNodeEffect.create(Color.TRANSPARENT, 0F).color(RectNode::computeBorderColor);
		super.effect(created);
		return created;
	}

	private @NonNull Color computeBorderColor() {
		return super.hoverColor(this.borderColor, this.hoveredBorderColor);
	}

}