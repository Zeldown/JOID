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
	private double dragProgress;

	protected BaseScrollbarNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		this.minThumb(() -> Math.min(super.getWidth(), super.getHeight()));

		super.<BaseScrollbarNode>require("scrolled node", "pass it to node.scrollbar(...) instead of attaching it", node -> node.scrollNode != null);
	}

	@Override
	public void placeParts(final double mouseX, final double mouseY) {
		if (!this.dragging || !this.isOverflowing()) {
			return;
		}

		final double room = this.getLength() - this.getThumbLength();
		final double start = (this.isHorizontal() ? mouseX - super.getAbsoluteX() : mouseY - super.getAbsoluteY()) - this.grab;
		this.dragProgress = room <= 0D ? 0D : Math.min(1D, Math.max(0D, start / room));
		if (this.isHorizontal()) {
			this.scrollNode.scrollTo(-this.scrollNode.getMaxScrollX() * this.dragProgress, this.scrollNode.getTargetScrollY());
		} else {
			this.scrollNode.scrollTo(this.scrollNode.getTargetScrollX(), -this.scrollNode.getMaxScrollY() * this.dragProgress);
		}
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {
		this.draw(mouseX, mouseY);
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (this.dragging || !event.getButton().isLeft() || !super.isHovered()) {
			return;
		}

		final double length = this.getThumbLength();
		final double start = (this.getLength() - length) * this.getProgress();
		final double mouse = this.isHorizontal() ? event.getMouseX() - super.getAbsoluteX() : event.getMouseY() - super.getAbsoluteY();
		this.grab         = mouse >= start && mouse <= start + length ? mouse - start : length / 2D;
		this.dragProgress = this.getProgress();
		this.dragging     = true;
		event.consume();
	}

	@Override
	public void mouseReleased(final @NonNull NodeMouseEvent<?> event) {
		if (event.getButton().isLeft()) {
			this.dragging = false;
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

	public final double getProgress() {
		if (!this.isOverflowing()) {
			return 0D;
		}

		if (this.dragging) {
			return this.dragProgress;
		}

		final double scroll = this.isHorizontal() ? this.scrollNode.getScrollX() / this.scrollNode.getMaxScrollX() : this.scrollNode.getScrollY() / this.scrollNode.getMaxScrollY();
		return Math.min(1D, Math.max(0D, Math.abs(scroll)));
	}

	public final double getRatio() {
		if (!this.isOverflowing()) {
			return 1D;
		}

		final double visible = this.isHorizontal() ? this.scrollNode.getWidth() : this.scrollNode.getHeight();
		return visible / (visible + (this.isHorizontal() ? this.scrollNode.getMaxScrollX() : this.scrollNode.getMaxScrollY()));
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

	private double getLength() {
		return this.isHorizontal() ? super.getWidth() : super.getHeight();
	}

}