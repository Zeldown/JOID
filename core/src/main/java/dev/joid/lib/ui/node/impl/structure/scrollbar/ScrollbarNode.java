package dev.joid.lib.ui.node.impl.structure.scrollbar;

import java.lang.reflect.Constructor;

import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.box.BoundingBox;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class ScrollbarNode extends Node {

	@NonNull
	private final BoundingBox scroll;

	private Node        scrollNode;
	private boolean     dragging;
	private MouseButton dragButton;

	protected ScrollbarNode(final double x, final double y, final double width, final double height, final @NonNull BoundingBox scroll) {
		super(x, y, width, height);
		this.scroll = scroll;

		super.<ScrollbarNode>require("scrolled node", "pass it to node.scrollbar(...) instead of attaching it", node -> node.scrollNode != null);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.dragging) {
			if (this.isHorizontal() && this.scrollNode.hasOverflowX()) {
				final double newX = Math.min(Math.max(super.getDefaultX(), mouseX - super.getAbsoluteDefaultX() + super.getDefaultX() - super.dw(2D)), super.getDefaultX() + this.getScrollWidth());
				super.layoutPosition(newX - super.getDefaultX(), 0D);

				final double percent = (newX - super.getDefaultX()) / this.getScrollWidth();
				this.scrollNode.scrollTo(-this.scrollNode.getMaxScrollX() * percent, this.scrollNode.getTargetScrollY());
			} else if (!this.isHorizontal() && this.scrollNode.hasOverflowY()) {
				final double newY = Math.min(Math.max(super.getDefaultY(), mouseY - super.getAbsoluteDefaultY() + super.getDefaultY() - super.dh(2D)), super.getDefaultY() + this.getScrollHeight());
				super.layoutPosition(0D, newY - super.getDefaultY());

				final double percent = (newY - super.getDefaultY()) / this.getScrollHeight();
				this.scrollNode.scrollTo(this.scrollNode.getTargetScrollX(), -this.scrollNode.getMaxScrollY() * percent);
			}
		}

		this.drawScrollbar(mouseX, mouseY);
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

		this.dragging   = true;
		this.dragButton = event.getButton();
		event.consume();
	}

	@Override
	public void mouseReleased(final @NonNull NodeMouseEvent<?> event) {
		if (event.getButton() == this.dragButton) {
			this.dragging   = false;
			this.dragButton = null;
		}
	}

	@Override
	public void detach() {
		this.dragging   = false;
		this.dragButton = null;
	}

	@Override
	protected @NonNull Node instantiate() {
		try {
			final Constructor<? extends ScrollbarNode> constructor = this.getClass().getDeclaredConstructor(double.class, double.class, double.class, double.class, BoundingBox.class);
			constructor.setAccessible(true);
			return constructor.newInstance(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.scroll);
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException("The scrollbar " + this.getClass().getSimpleName() + " cannot be copied (" + exception + "): declare a constructor (x, y, width, height, scroll) or override instantiate()", exception);
		}
	}

	public final <T extends ScrollbarNode> @NonNull T scrollNode(final @NonNull Node scrollNode) {
		this.scrollNode = scrollNode;
		return (T) this;
	}

	public final boolean isHorizontal() {
		return this.scroll.getWidth() > this.scroll.getHeight();
	}

	public final double getScrollWidth() {
		return this.scroll.getWidth() - super.getWidth();
	}

	public final double getScrollHeight() {
		return this.scroll.getHeight() - super.getHeight();
	}

	public abstract void drawScrollbar(final double mouseX, final double mouseY);
}