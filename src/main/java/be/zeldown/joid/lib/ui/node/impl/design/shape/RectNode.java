package be.zeldown.joid.lib.ui.node.impl.design.shape;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class RectNode extends Node {

	private Color color;
	private Color hoveredColor;

	private Color   borderColor;
	private Color   hoveredBorderColor;
	private double  borderStroke;
	private boolean borderFill;

	private float borderRadius;

	protected RectNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.color = Color.WHITE;
	}

	public static @NonNull RectNode create(final double x, final double y, final double width, final double height) {
		return new RectNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.borderRadius > 0F) {
			this.drawBorderPre();
			DrawUtils.SHAPE.drawRoundedRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.hoveredColor != null ? this.color.to(this.hoveredColor, super.hoverValue(1F)) : this.color, this.borderRadius);
			this.drawBorderPost();
			return;
		}

		this.drawBorderPre();
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.hoveredColor != null ? this.color.to(this.hoveredColor, super.hoverValue(1F)) : this.color);
		this.drawBorderPost();
	}

	private final void drawBorderPre() {
		if (this.borderRadius <= 0F || this.borderStroke <= 0.0D || this.borderColor == null) {
			return;
		}

		DrawUtils.SHAPE.drawRoundedRect(super.getX() - this.borderStroke, super.getY() - this.borderStroke, super.getWidth() + this.borderStroke * 2, super.getHeight() + this.borderStroke * 2, this.hoveredBorderColor != null ? this.borderColor.to(this.hoveredBorderColor, super.hoverValue(1.0F)) : this.borderColor, (float)(this.borderRadius + this.borderStroke));
	}

	private final void drawBorderPost() {
		if (this.borderRadius > 0F || this.borderStroke <= 0.0D || this.borderColor == null) {
			return;
		}

		if (this.borderFill) {
			DrawUtils.SHAPE.drawFilledBorder(super.getX(), super.getY(), super.getX() + super.getWidth(), super.getY() + super.getHeight(), this.hoveredBorderColor != null ? this.borderColor.to(this.hoveredBorderColor, super.hoverValue(1.0F)) : this.borderColor, this.borderStroke);
		} else {
			DrawUtils.SHAPE.drawBorder(super.getX(), super.getY(), super.getX() + super.getWidth(), super.getY() + super.getHeight(), this.hoveredBorderColor != null ? this.borderColor.to(this.hoveredBorderColor, super.hoverValue(1.0F)) : this.borderColor, this.borderStroke);
		}
	}

	public final <T extends RectNode> @NonNull T color(final @NonNull Color color) {
		this.color = color;
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T color(final @NonNull Color color, final @NonNull Color hoveredColor) {
		this.color        = color;
		this.hoveredColor = hoveredColor;
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T hoveredColor(final @NonNull Color color) {
		this.hoveredColor = color;
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T border(final @NonNull Color border, final double stroke) {
		return this.border(border, stroke, true);
	}

	public final <T extends RectNode> @NonNull T border(final @NonNull Color color, final double stroke, final boolean fill) {
		this.borderColor  = color;
		this.borderStroke = stroke;
		this.borderFill   = fill;
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T border(final @NonNull Color color, final @NonNull Color hoveredColor, final double stroke, final boolean fill) {
		this.borderColor        = color;
		this.hoveredBorderColor = hoveredColor;
		this.borderStroke       = stroke;
		this.borderFill         = fill;
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T hoveredBorderColor(final @NonNull Color borderColor) {
		this.hoveredBorderColor = borderColor;
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T borderRadius(final float borderRadius) {
		this.borderRadius = borderRadius;
		return (T) this;
	}

}