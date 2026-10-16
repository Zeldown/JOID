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

	private double marginVertical;
	private double marginHorizontal;

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
	public void layout() {
		this.updateGrid();
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {}

	@Override
	public void update() {
		this.updateGrid();
	}

	public final @NonNull GridNode marginVertical(final double marginVertical) {
		return this.marginVertical(Signal.from(marginVertical));
	}

	public final @NonNull GridNode marginVertical(final @NonNull Supplier<Double> marginVertical) {
		return super.follow("marginVertical", marginVertical, value -> this.marginVertical = value);
	}

	public final @NonNull GridNode marginHorizontal(final double marginHorizontal) {
		return this.marginHorizontal(Signal.from(marginHorizontal));
	}

	public final @NonNull GridNode marginHorizontal(final @NonNull Supplier<Double> marginHorizontal) {
		return super.follow("marginHorizontal", marginHorizontal, value -> this.marginHorizontal = value);
	}

	public final @NonNull GridNode margin(final double margin) {
		return this.margin(Signal.from(margin));
	}

	public final @NonNull GridNode margin(final @NonNull Supplier<Double> margin) {
		this.marginVertical(margin);
		return this.marginHorizontal(margin);
	}

	private void updateGrid() {
		double ox = 0D;
		double oy = 0D;
		double rowHeight = 0D;
		for (final Node child : super.getChildren()) {
			if (child.getPosition() == PositionProperty.ABSOLUTE || !child.isVisibleProperty()) {
				continue;
			}

			if (ox > 0D && child.getDefaultX() + child.getWidth() + ox > super.getWidth()) {
				ox = 0D;
				oy += rowHeight + this.marginVertical;
				rowHeight = 0D;
			}

			child.layoutPosition(ox, oy);

			ox += child.getWidth() + this.marginHorizontal;
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