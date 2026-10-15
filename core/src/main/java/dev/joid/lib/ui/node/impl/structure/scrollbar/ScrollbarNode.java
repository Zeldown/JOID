package dev.joid.lib.ui.node.impl.structure.scrollbar;

import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class ScrollbarNode extends Node {

	private Node track;
	private Node thumb;
	private Node scrollNode;

	private boolean dragging;

	protected ScrollbarNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		super.<ScrollbarNode>require("scrolled node", "pass it to node.scrollbar(...) instead of attaching it", node -> node.scrollNode != null);
		super.<ScrollbarNode>require("thumb", "give it one with thumb(...)", node -> node.thumb != null);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.track != null) {
			this.track.layoutPosition(0D, 0D).layoutSize(super.getWidth(), super.getHeight());
		}

		final boolean horizontal = this.isHorizontal();
		this.thumb.fit(horizontal ? super.getHeight() : super.getWidth(), horizontal ? super.getHeight() : super.getWidth());
		final double room = horizontal ? super.getWidth() - this.thumb.getWidth() : super.getHeight() - this.thumb.getHeight();
		final boolean overflows = horizontal ? this.scrollNode.hasOverflowX() : this.scrollNode.hasOverflowY();
		double offset = 0D;
		if (this.dragging && overflows && room > 0D) {
			offset = Math.min(Math.max(0D, horizontal ? mouseX - super.getAbsoluteX() - this.thumb.getWidth() / 2D : mouseY - super.getAbsoluteY() - this.thumb.getHeight() / 2D), room);
			if (horizontal) {
				this.scrollNode.scrollTo(-this.scrollNode.getMaxScrollX() * offset / room, this.scrollNode.getTargetScrollY());
			} else {
				this.scrollNode.scrollTo(this.scrollNode.getTargetScrollX(), -this.scrollNode.getMaxScrollY() * offset / room);
			}
		} else if (overflows) {
			final double scroll = horizontal ? this.scrollNode.getScrollX() / this.scrollNode.getMaxScrollX() : this.scrollNode.getScrollY() / this.scrollNode.getMaxScrollY();
			offset = room * Math.min(1D, Math.max(0D, Math.abs(scroll)));
		}

		this.thumb.layoutPosition(horizontal ? offset : 0D, horizontal ? 0D : offset);
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {
		this.draw(mouseX, mouseY);
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (this.dragging || !event.getButton().isLeft() || !this.thumb.isHovered()) {
			return;
		}

		this.dragging = true;
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

	public final <T extends ScrollbarNode> @NonNull T track(final @NonNull Node track) {
		this.track = super.part(this.track, track);
		return (T) this;
	}

	public final <T extends ScrollbarNode> @NonNull T thumb(final @NonNull Node thumb) {
		this.thumb = super.part(this.thumb, thumb);
		return (T) this;
	}

	public final <T extends ScrollbarNode> @NonNull T scrollNode(final @NonNull Node scrollNode) {
		this.scrollNode = scrollNode;
		return (T) this;
	}

}