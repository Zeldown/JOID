package dev.joid.lib.ui.node.impl.structure.flex;

import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.ui.node.property.position.PositionProperty;
import dev.joid.lib.utils.align.Align;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class FlexNode extends Node {

	private Align align;
	private FlexDirection direction;

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
	public void draw(final double mouseX, final double mouseY) {
		this.updateFlex();
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {
		this.updateFlex();
	}

	@Override
	public void update() {
		this.updateFlex();
	}

	@Override
	public @NonNull Node instantiate() {
		return new FlexNode(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.direction);
	}

	public final @NonNull FlexNode direction(final @NonNull FlexDirection direction) {
		return this.direction(Signal.from(direction));
	}

	public final @NonNull FlexNode direction(final @NonNull Supplier<@NonNull FlexDirection> direction) {
		return super.follow("direction", direction, value -> this.changeDirection(value));
	}

	public final @NonNull FlexNode align(final Align align) {
		return this.align(Signal.from(align));
	}

	public final @NonNull FlexNode align(final @NonNull Supplier<Align> align) {
		return super.follow("align", align, value -> this.align = value);
	}

	public final @NonNull FlexNode margin(final double margin) {
		return this.margin(Signal.from(margin));
	}

	public final @NonNull FlexNode margin(final @NonNull Supplier<Double> margin) {
		return super.follow("margin", margin, value -> this.margin = value);
	}

	private final void changeDirection(final FlexDirection direction) {
		if (this.direction != direction) {
			for (final Node child : super.getChildren()) {
				child.layoutPosition(0D, 0D);
			}
		}

		this.direction = direction;
	}

	private final void updateFlex() {
		double offset = 0D;
		for (final Node child : super.getChildren()) {
			if (child.getPosition() == PositionProperty.ABSOLUTE || !child.isVisibleProperty()) {
				continue;
			}

			if (this.direction == FlexDirection.COLUMN) {
				child.layoutPosition(this.align == null ? 0D : this.getCrossX(child) - child.getDefaultX(), offset);
				offset += child.getHeight() + this.margin;
			} else {
				child.layoutPosition(offset, this.align == null ? 0D : this.getCrossY(child) - child.getDefaultY());
				offset += child.getWidth() + this.margin;
			}
		}

		if (super.getOverflow() != OverflowProperty.NONE) {
			return;
		}

		if (this.direction == FlexDirection.COLUMN) {
			super.layoutSize(super.getWidth(), Math.max(0, offset - this.margin));
		} else {
			super.layoutSize(Math.max(0, offset - this.margin), super.getHeight());
		}
	}

	private final double getCrossX(final Node child) {
		return this.align.isCenter() ? super.dw(2) - child.dw(2) : this.align.isEnd() ? super.aw(-child.getWidth()) : 0D;
	}

	private final double getCrossY(final Node child) {
		return this.align.isCenter() ? super.dh(2) - child.dh(2) : this.align.isEnd() ? super.ah(-child.getHeight()) : 0D;
	}

	public static enum FlexDirection {

		COLUMN,
		ROW;

	}

}