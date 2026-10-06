package dev.joid.lib.ui.node.impl.structure.scrollbar;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.box.BoundingBox;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class ScrollbarNode extends Node {

	@NonNull
	private final BoundingBox scroll;

	private Node      scrollNode;
	private boolean   dragging;
	private ClickType dragButton;

	protected ScrollbarNode(final double x, final double y, final double width, final double height, final @NonNull BoundingBox scroll) {
		super(x, y, width, height);
		this.scroll = scroll;
	}

	@Override
	public final void draw(final double mouseX, final double mouseY) {
		if (this.scrollNode == null) {
			return;
		}

		if (this.dragging) {
			if (this.isHorizontal() && this.scrollNode.hasOverflowX()) {
				final double newX = Math.min(Math.max(super.getDefaultX(), mouseX - super.getAbsoluteDefaultX() + super.getDefaultX() - super.dw(2D)), super.getDefaultX() + this.getScrollWidth());
				super.x(newX);

				final float percent = (float) ((newX - super.getDefaultX()) / this.getScrollWidth());
				this.scrollNode.scrollRatioX(percent);
			} else if (!this.isHorizontal() && this.scrollNode.hasOverflowY()) {
				final double newY = Math.min(Math.max(super.getDefaultY(), mouseY - super.getAbsoluteDefaultY() + super.getDefaultY() - super.dh(2D)), super.getDefaultY() + this.getScrollHeight());
				super.y(newY);

				final float percent = (float) ((newY - super.getDefaultY()) / this.getScrollHeight());
				this.scrollNode.scrollRatioY(percent);
			}
		}

		this.drawScrollbar(mouseX, mouseY);
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {
		this.draw(mouseX, mouseY);
	}

	@Override
	public final void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		if (this.dragging || !this.isHovered(mouseX, mouseY)) {
			return;
		}

		context.cancel(() -> {
			this.dragging   = true;
			this.dragButton = clickType;
		});
	}

	@Override
	public final void mouseReleased(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		if (clickType == this.dragButton) {
			this.dragging   = false;
			this.dragButton = null;
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