package be.zeldown.joid.lib.ui.node.impl.design.shape;

import java.util.function.Supplier;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.effect.NodeEffect;
import be.zeldown.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class RectNode extends Node {

	private Supplier<Color> color;
	private Supplier<Color> hoveredColor;

	private Supplier<Color> borderColor;
	private Supplier<Color> hoveredBorderColor;
	private double          borderStroke;
	private boolean         borderFill;

	protected RectNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.color = () -> Color.TRANSPARENT;
		this.hoveredColor = null;

		this.borderColor = () -> Color.TRANSPARENT;
		this.hoveredBorderColor = null;
	}

	public static @NonNull RectNode create(final double x, final double y, final double width, final double height) {
		return new RectNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final Color hoveredColor = this.hoveredColor != null ? this.hoveredColor.get() : null;
		final Color color = hoveredColor != null ? this.color.get().to(hoveredColor, super.hoverValue(1F)) : this.color.get();
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), color);
	}

	/* [ Getter Section ] */
	public final @NonNull Color getColor() {
		return this.color.get();
	}

	public final @NonNull Color getHoveredColor() {
		return this.hoveredColor.get();
	}

	public final @NonNull Color getBorderColor() {
		return this.borderColor.get();
	}

	public final @NonNull Color getHoveredBorderColor() {
		return this.hoveredBorderColor.get();
	}

	/* [ Setter Section ] */
	public final <T extends RectNode> @NonNull T color(final @NonNull Color color) {
		this.color(() -> color);
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T color(final @NonNull Color color, final Color hoveredColor) {
		this.color(() -> color, () -> hoveredColor);
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T hoveredColor(final Color color) {
		this.hoveredColor(() -> color);
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T border(final @NonNull Color border, final double stroke) {
		return this.border(() -> border, stroke);
	}

	public final <T extends RectNode> @NonNull T border(final @NonNull Color color, final double stroke, final boolean fill) {
		return this.border(() -> color, stroke, fill);
	}

	public final <T extends RectNode> @NonNull T border(final @NonNull Color color, final Color hoveredColor, final double stroke, final boolean fill) {
		return this.border(() -> color, () -> hoveredColor, stroke, fill);
	}

	public final <T extends RectNode> @NonNull T hoveredBorderColor(final Color hoveredColor) {
		this.hoveredBorderColor(() -> hoveredColor);
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T color(final @NonNull Supplier<@NonNull Color> color) {
		this.color = color;
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T color(final @NonNull Supplier<@NonNull Color> color, final Supplier<Color> hoveredColor) {
		this.color        = color;
		this.hoveredColor = hoveredColor;
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T hoveredColor(final Supplier<Color> color) {
		this.hoveredColor = color;
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T border(final @NonNull Supplier<@NonNull Color> border, final double stroke) {
		return this.border(border, stroke, true);
	}

	public final <T extends RectNode> @NonNull T border(final @NonNull Supplier<@NonNull Color> color, final double stroke, final boolean fill) {
		this.borderColor  = color;
		this.borderStroke = stroke;
		this.borderFill   = fill;
		this.applyBorderEffect();
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T border(final @NonNull Supplier<@NonNull Color> color, final Supplier<Color> hoveredColor, final double stroke, final boolean fill) {
		this.borderColor        = color;
		this.hoveredBorderColor = hoveredColor;
		this.borderStroke       = stroke;
		this.borderFill         = fill;
		this.applyBorderEffect();
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T hoveredBorderColor(final Supplier<Color> hoveredColor) {
		this.hoveredBorderColor = hoveredColor;
		return (T) this;
	}

	/* [ Internal Section ] */
	private void applyBorderEffect() {
		super.removeEffect((Class<? extends NodeEffect<?>>) (Class<?>) BorderNodeEffect.class);
		if (this.borderStroke > 0D) {
			final BorderNodeEffect<Node> effect = BorderNodeEffect.create(Color.TRANSPARENT, (float) this.borderStroke);
			effect.color(this::computeBorderColor);
			effect.fill(this.borderFill);
			super.effect(effect);
		}
	}

	private Color computeBorderColor() {
		final Color hovered = this.hoveredBorderColor != null ? this.hoveredBorderColor.get() : null;
		return hovered != null ? this.borderColor.get().to(hovered, super.hoverValue(1F)) : this.borderColor.get();
	}

}