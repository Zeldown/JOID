package dev.joid.lib.ui.node.impl.layout;

import java.util.List;
import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.property.OverflowProperty;
import dev.joid.lib.ui.node.property.PositionProperty;
import dev.joid.lib.utils.align.Align;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class GridNode extends Node {

	private Align alignX;

	private double marginHorizontal;
	private double marginVertical;

	private GridNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.alignX = Align.START;
	}

	public static @NonNull GridNode create(final double x, final double y, final double width, final double height) {
		return new GridNode(x, y, width, height);
	}

	@Override
	public void init(final @NonNull UI ui) {
		this.updateGrid();
	}

	@Override
	public void update() {
		this.updateGrid();
	}

	@Override
	public void layout() {
		this.updateGrid();
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {}

	public @NonNull GridNode alignX(final @NonNull Align alignX) {
		return this.alignX(Signal.from(alignX));
	}

	public @NonNull GridNode alignX(final @NonNull Supplier<@NonNull Align> alignX) {
		return super.follow("alignX", alignX, value -> this.alignX = value);
	}

	public @NonNull GridNode margin(final double margin) {
		return this.margin(Signal.from(margin));
	}

	public @NonNull GridNode margin(final @NonNull Supplier<Double> margin) {
		this.marginVertical(margin);
		return this.marginHorizontal(margin);
	}

	public @NonNull GridNode marginHorizontal(final double marginHorizontal) {
		return this.marginHorizontal(Signal.from(marginHorizontal));
	}

	public @NonNull GridNode marginHorizontal(final @NonNull Supplier<Double> marginHorizontal) {
		return super.follow("marginHorizontal", marginHorizontal, value -> this.marginHorizontal = value);
	}

	public @NonNull GridNode marginVertical(final double marginVertical) {
		return this.marginVertical(Signal.from(marginVertical));
	}

	public @NonNull GridNode marginVertical(final @NonNull Supplier<Double> marginVertical) {
		return super.follow("marginVertical", marginVertical, value -> this.marginVertical = value);
	}

	private void updateGrid() {
		final List<Node> children = super.getChildren();
		int rowStart = 0;
		double ox = 0D;
		double oy = 0D;
		double rowWidth = 0D;
		double rowHeight = 0D;
		for (int i = 0; i < children.size(); i++) {
			final Node child = children.get(i);
			if (!this.isLaidOut(child)) {
				continue;
			}

			if (ox > 0D && child.getDefaultX() + child.getWidth() + ox > super.getWidth()) {
				this.alignRow(children.subList(rowStart, i), rowWidth);
				rowStart = i;
				ox = 0D;
				oy += rowHeight + this.marginVertical;
				rowWidth = 0D;
				rowHeight = 0D;
			}

			child.layoutPosition(ox, oy);

			rowWidth = Math.max(rowWidth, ox + child.getWidth());
			ox += child.getWidth() + this.marginHorizontal;
			rowHeight = Math.max(rowHeight, child.getHeight());
		}

		this.alignRow(children.subList(rowStart, children.size()), rowWidth);
		oy += rowHeight;

		if (super.getDefaultHeight() == 0D) {
			super.layoutSize(super.getWidth(), oy);
		} else if (super.getOverflow() == OverflowProperty.NONE) {
			super.layoutSize(super.getWidth(), Math.max(oy, super.getDefaultHeight()));
		}
	}

	private void alignRow(final List<Node> row, final double rowWidth) {
		if (this.alignX.isStart()) {
			return;
		}

		final double shift = this.alignX.isCenter() ? super.dw(2D) - rowWidth / 2D : super.aw(-rowWidth);
		for (final Node child : row) {
			if (this.isLaidOut(child)) {
				child.layoutPosition(child.getLayoutX() + shift, child.getLayoutY());
			}
		}
	}

	private boolean isLaidOut(final Node child) {
		return child.getPosition() != PositionProperty.ABSOLUTE && child.isVisibleProperty();
	}

}