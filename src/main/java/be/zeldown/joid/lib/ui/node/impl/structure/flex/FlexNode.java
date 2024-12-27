package be.zeldown.joid.lib.ui.node.impl.structure.flex;

import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.utils.align.Align;
import lombok.NonNull;

public final class FlexNode extends Node {

	private FlexDirection direction;
	private Align align;

	private double margin;

	private FlexNode(final double x, final double y, final double width, final double height, final @NonNull FlexDirection direction) {
		super(x, y, width, height);

		this.direction = direction;
		this.align     = null;
		this.margin    = 0D;
	}

	public static final @NonNull FlexNode vertical(final double x, final double y, final double width) {
		return new FlexNode(x, y, width, 0, FlexDirection.COLUMN);
	}

	public static final @NonNull FlexNode horizontal(final double x, final double y, final double height) {
		return new FlexNode(x, y, 0, height, FlexDirection.ROW);
	}

	@Override
	public void init(final @NonNull UI ui) {
		this.updateFlex();
	}

	@Override
	public final void draw(final double mouseX, final double mouseY) {
		this.updateFlex();
	}

	@Override
	public final void drawSkeleton(final double mouseX, final double mouseY) {
		this.updateFlex();
	}

	@Override
	public void update() {
		this.updateFlex();
	}

	private final void updateFlex() {
		if (this.direction == FlexDirection.COLUMN) {
			double offsetY = 0D;
			for (final Node child : super.getChildren()) {
				child.y(child.getDefaultY() + offsetY);
				if (child.isVisibleProperty()) {
					offsetY += child.getHeight() + this.margin;
				}

				if (this.align != null) {
					if (this.align.isStart()) {
						child.x(0);
					} else if (this.align.isCenter()) {
						child.x(super.dw(2) - child.dw(2));
					} else if (this.align.isEnd()) {
						child.x(super.aw(-child.getWidth()));
					}
				}
			}

			super.height(Math.max(0, offsetY - this.margin));
		} else if (this.direction == FlexDirection.ROW) {
			double offsetX = 0D;
			for (final Node child : super.getChildren()) {
				child.x(child.getDefaultX() + offsetX);
				if (child.isVisibleProperty()) {
					offsetX += child.getWidth() + this.margin;
				}

				if (this.align != null) {
					if (this.align.isStart()) {
						child.y(0);
					} else if (this.align.isCenter()) {
						child.y(super.dh(2) - child.dh(2));
					} else if (this.align.isEnd()) {
						child.y(super.ah(-child.getHeight()));
					}
				}
			}

			super.width(Math.max(0, offsetX - this.margin));
		}
	}

	public final @NonNull FlexNode direction(final @NonNull FlexDirection direction) {
		this.direction = direction;
		return this;
	}

	public final @NonNull FlexNode align(final Align align) {
		this.align = align;
		return this;
	}

	public final @NonNull FlexNode margin(final double margin) {
		this.margin = margin;
		return this;
	}

	public static enum FlexDirection {

		COLUMN,
		ROW;

	}

}