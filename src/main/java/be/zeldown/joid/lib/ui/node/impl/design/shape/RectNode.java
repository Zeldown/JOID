package be.zeldown.joid.lib.ui.node.impl.design.shape;

import java.util.function.Supplier;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.effect.NodeEffect;
import be.zeldown.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import be.zeldown.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
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

		final Color hoveredBorderColor = this.hoveredBorderColor != null ? this.hoveredBorderColor.get() : null;
		final Color borderColor = hoveredBorderColor != null ? this.borderColor.get().to(hoveredBorderColor, super.hoverValue(1F)) : this.borderColor.get();

		final RoundedNodeEffect<?> roundedEffect = super.getEffect(RoundedNodeEffect.class);
		if (roundedEffect != null && roundedEffect.getRadius() > 0F) {
			final float borderRadius = roundedEffect.getRadius();
			final boolean roundedLeft = roundedEffect.isLeft();
			final boolean roundedTop = roundedEffect.isTop();
			final boolean roundedRight = roundedEffect.isRight();
			final boolean roundedBottom = roundedEffect.isBottom();

			if (this.borderStroke > 0D) {
				DrawUtils.SHAPE.drawRoundedRect(super.getX() - this.borderStroke, super.getY() - this.borderStroke, super.getWidth() + this.borderStroke * 2, super.getHeight() + this.borderStroke * 2, borderColor, (float) (borderRadius + this.borderStroke), roundedLeft, roundedTop, roundedRight, roundedBottom);
			}
			DrawUtils.SHAPE.drawRoundedRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), color, borderRadius, roundedLeft, roundedTop, roundedRight, roundedBottom);
			return;
		}

		final CircleNodeEffect<?> circleEffect = super.getEffect(CircleNodeEffect.class);
		if (circleEffect != null) {
			final float size = (float) Math.min(super.dw(2), super.dh(2));
			if (this.borderStroke > 0D) {
				DrawUtils.SHAPE.drawCircle(super.ax(super.dw(2)), super.ay(super.dh(2)), borderColor, size + this.borderStroke * 2);
			}
			DrawUtils.SHAPE.drawCircle(super.ax(super.dw(2)), super.ay(super.dh(2)), color, size);
			return;
		}

		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), color);
		if (this.borderStroke > 0D) {
			if (this.borderFill) {
				DrawUtils.SHAPE.drawFilledBorder(super.getX(), super.getY(), super.getX() + super.getWidth(), super.getY() + super.getHeight(), borderColor, this.borderStroke);
			} else {
				DrawUtils.SHAPE.drawBorder(super.getX(), super.getY(), super.getX() + super.getWidth(), super.getY() + super.getHeight(), borderColor, this.borderStroke);
			}
		}
	}

	/* [ Override Section ] */
	@Override
	public boolean shouldApplyEffect(final @NonNull NodeEffect<Node> effect) {
		return super.shouldApplyEffect(effect) && !(effect instanceof RoundedNodeEffect) && !(effect instanceof CircleNodeEffect);
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
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T border(final @NonNull Supplier<@NonNull Color> color, final Supplier<Color> hoveredColor, final double stroke, final boolean fill) {
		this.borderColor        = color;
		this.hoveredBorderColor = hoveredColor;
		this.borderStroke       = stroke;
		this.borderFill         = fill;
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T hoveredBorderColor(final Supplier<Color> hoveredColor) {
		this.hoveredBorderColor = hoveredColor;
		return (T) this;
	}

}