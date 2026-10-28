package dev.joid.lib.ui.node.impl.control.scrollbar;

import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

public abstract class BaseScrollbarNode extends Node {

	@Getter private Node    scrollNode;
	@Getter private double  minThumb;
	@Getter private boolean dragging;

	private double grab;

	protected BaseScrollbarNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		this.minThumb(() -> Math.min(super.getWidth(), super.getHeight()));

		super.<BaseScrollbarNode>require("scrolled node", "pass it to node.scrollbar(...) instead of attaching it", node -> node.scrollNode != null);
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {
		this.draw(mouseX, mouseY);
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (this.dragging || event.isConsumed() || !event.getButton().isLeft() || !super.isHovered()) {
			return;
		}

		final double length = this.getThumbLength();
		final double start = (this.getLength() - length) * this.getProgress();
		final double mouse = this.isHorizontal() ? event.getMouseX() - super.getAbsoluteX() : event.getMouseY() - super.getAbsoluteY();
		this.grab     = mouse >= start && mouse <= start + length ? mouse - start : length / 2D;
		this.dragging = true;
		this.slide(event.getMouseX(), event.getMouseY());
		event.consume();
	}

	@Override
	public void mouseReleased(final @NonNull NodeMouseEvent<?> event) {
		if (event.getButton().isLeft()) {
			this.dragging = false;
		}
	}

	@Override
	public void mouseDragged(final @NonNull NodeMouseEvent<?> event) {
		if (this.dragging) {
			this.slide(event.getMouseX(), event.getMouseY());
		}
	}

	@Override
	public void detach() {
		this.dragging = false;
	}

	public final boolean isHorizontal() {
		return super.getWidth() > super.getHeight();
	}

	public final boolean isOverflowing() {
		return this.scrollNode != null && (this.isHorizontal() ? this.scrollNode.hasOverflowX() : this.scrollNode.hasOverflowY());
	}

	public final double getRatio() {
		if (!this.isOverflowing()) {
			return 1D;
		}

		final double visible = this.isHorizontal() ? this.scrollNode.getWidth() : this.scrollNode.getHeight();
		return visible / (visible + (this.isHorizontal() ? this.scrollNode.getMaxScrollX() : this.scrollNode.getMaxScrollY()));
	}

	public final double getProgress() {
		if (!this.isOverflowing()) {
			return 0D;
		}

		final double scroll = this.isHorizontal() ? (this.dragging ? this.scrollNode.getTargetScrollX() : this.scrollNode.getScrollX()) / this.scrollNode.getMaxScrollX() : (this.dragging ? this.scrollNode.getTargetScrollY() : this.scrollNode.getScrollY()) / this.scrollNode.getMaxScrollY();
		return Math.min(1D, Math.max(0D, Math.abs(scroll)));
	}

	public final double getThumbLength() {
		return Math.min(this.getLength(), Math.max(this.minThumb, this.getLength() * this.getRatio()));
	}

	public final <T extends BaseScrollbarNode> @NonNull T scrollNode(final @NonNull Node scrollNode) {
		this.scrollNode = scrollNode;
		return (T) this;
	}

	public final <T extends BaseScrollbarNode> @NonNull T minThumb(final double minThumb) {
		return this.minThumb(Signal.from(minThumb));
	}

	public final <T extends BaseScrollbarNode> @NonNull T minThumb(final @NonNull Supplier<Double> minThumb) {
		return super.follow("minThumb", minThumb, value -> this.minThumb = value);
	}

	private void slide(final double mouseX, final double mouseY) {
		if (!this.isOverflowing()) {
			return;
		}

		final double room = this.getLength() - this.getThumbLength();
		final double start = (this.isHorizontal() ? mouseX - super.getAbsoluteX() : mouseY - super.getAbsoluteY()) - this.grab;
		final double progress = room <= 0D ? 0D : Math.min(1D, Math.max(0D, start / room));
		if (this.isHorizontal()) {
			this.scrollNode.scrollTo(-this.scrollNode.getMaxScrollX() * progress, this.scrollNode.getTargetScrollY());
		} else {
			this.scrollNode.scrollTo(this.scrollNode.getTargetScrollX(), -this.scrollNode.getMaxScrollY() * progress);
		}
	}

	private double getLength() {
		return this.isHorizontal() ? super.getWidth() : super.getHeight();
	}

}