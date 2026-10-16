package dev.joid.lib.ui.node.impl.layout;

import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.property.OverflowProperty;
import dev.joid.lib.ui.node.property.PositionProperty;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class GridNode extends Node {

	private double verticalMargin;
	private double horizontalMargin;

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
	public void draw(final double mouseX, final double mouseY) {
		this.updateGrid();
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {
		this.updateGrid();
	}

	@Override
	public void update() {
		this.updateGrid();
	}

	public final @NonNull GridNode verticalMargin(final double verticalMargin) {
		return this.verticalMargin(Signal.from(verticalMargin));
	}

	public final @NonNull GridNode verticalMargin(final @NonNull Supplier<Double> verticalMargin) {
		return super.follow("verticalMargin", verticalMargin, value -> this.verticalMargin = value);
	}

	public final @NonNull GridNode horizontalMargin(final double horizontalMargin) {
		return this.horizontalMargin(Signal.from(horizontalMargin));
	}

	public final @NonNull GridNode horizontalMargin(final @NonNull Supplier<Double> horizontalMargin) {
		return super.follow("horizontalMargin", horizontalMargin, value -> this.horizontalMargin = value);
	}

	public final @NonNull GridNode margin(final double margin) {
		return this.margin(Signal.from(margin));
	}

	public final @NonNull GridNode margin(final @NonNull Supplier<Double> margin) {
		this.verticalMargin(margin);
		return this.horizontalMargin(margin);
	}

	private void updateGrid() {
		double ox = 0D;
		double oy = 0D;
		double rowHeight = 0D;
		for (final Node child : super.getChildren()) {
			if (child.getPosition() == PositionProperty.ABSOLUTE || !child.isVisibleProperty()) {
				continue;
			}

			if (ox > 0 && child.getDefaultX() + child.getWidth() + ox > super.getWidth()) {
				ox = 0;
				oy += rowHeight + this.verticalMargin;
				rowHeight = 0D;
			}

			child.layoutPosition(ox, oy);

			ox += child.getWidth() + this.horizontalMargin;
			rowHeight = Math.max(rowHeight, child.getHeight());
		}

		oy += rowHeight;

		if (super.getDefaultHeight() == 0D) {
			super.layoutSize(super.getWidth(), oy);
		} else if (super.getOverflow() == OverflowProperty.NONE) {
			super.layoutSize(super.getWidth(), Math.max(oy, super.getDefaultHeight()));
		}
	}

}