package be.zeldown.joid.lib.ui.node.impl.structure.grid;

import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.property.overflow.OverflowProperty;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class GridNode extends Node {

	private double verticalMargin;
	private double horizontalMargin;

	private GridNode(final double x, final double y, final int rows, final int columns) {
		super(x, y);
	}

	private GridNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull GridNode create(final double x, final double y, final double width, final double height) {
		return new GridNode(x, y, width, height);
	}

	@Override
	public void init(final @NonNull UI ui) {
		this.updateGrid();
	}

	@Override
	public final void draw(final double mouseX, final double mouseY) {
		this.updateGrid();
	}

	@Override
	public final void drawSkeleton(final double mouseX, final double mouseY) {
		this.updateGrid();
	}

	@Override
	public void update() {
		this.updateGrid();
	}

	private final void updateGrid() {
		if (super.getChildren().isEmpty()) {
			return;
		}

		double ox = 0;
		double oy = 0;
		for (final Node child : super.getChildren()) {
			if (child.getDefaultX() + child.getWidth() + ox >= super.getWidth()) {
				ox = 0;
				oy += child.getHeight() + this.verticalMargin;
			}

			child.y(child.getDefaultY() + oy);
			child.x(child.getDefaultX() + ox);

			ox += child.getWidth() + this.horizontalMargin;
		}

		oy += super.getChildren().ordered().get(super.getChildren().size() - 1).getHeight();
		if (this.getOverflow() == OverflowProperty.NONE && oy > super.getDefaultHeight()) {
			super.height(oy);
		}
	}

	public final @NonNull GridNode verticalMargin(final double verticalMargin) {
		this.verticalMargin = verticalMargin;
		return this;
	}

	public final @NonNull GridNode horizontalMargin(final double horizontalMargin) {
		this.horizontalMargin = horizontalMargin;
		return this;
	}

	public final @NonNull GridNode margin(final double margin) {
		this.verticalMargin   = margin;
		this.horizontalMargin = margin;
		return this;
	}

}