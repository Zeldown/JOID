package be.zeldown.joid.lib.ui.node.impl.structure.scrollbar;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@SuppressWarnings("unchecked")
public abstract class ScrollbarNode extends Node {

	@NonNull private final ScrollbarBounds scroll;

	private boolean dragging;
	private Node scrollNode;

	protected ScrollbarNode(final double x, final double y, final double width, final double height, final @NonNull ScrollbarBounds scroll) {
		super(x, y, width, height);
		this.scroll = scroll;
	}

	@Override
	public final void draw(final double mouseX, final double mouseY) {
		if (this.scrollNode == null) {
			return;
		}

		if (this.dragging) {
			if (this.scrollNode.hasOverflowX()) {
				final double newX = Math.min(Math.max(super.getDefaultX(), mouseX - super.getAbsoluteDefaultX() + super.getDefaultX() - super.dw(2D)), super.getDefaultX() + this.getScrollWidth());
				super.x(newX);

				final float percent = (float) ((newX - super.getDefaultX()) / this.getScrollWidth());
				this.scrollNode.setScrollX(percent);
			} else if (this.scrollNode.hasOverflowY()) {
				final double newY = Math.min(Math.max(super.getDefaultY(), mouseY - super.getAbsoluteDefaultY() + super.getDefaultY() - super.dh(2D)), super.getDefaultY() + this.getScrollHeight());
				super.y(newY);

				final float percent = (float) ((newY - super.getDefaultY()) / this.getScrollHeight());
				this.scrollNode.setScrollY(percent);
			}
		}

		this.drawScrollbar(mouseX, mouseY);
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {
		this.draw(mouseX, mouseY);
	}

	@Override
	public final void mousePressed(final double mouseX, final double mouseY, final int clickType, final @NonNull InternalContext context) {
		if (!this.isHovered(mouseX, mouseY)) {
			return;
		}

		context.cancel(() -> this.dragging = true);
	}

	@Override
	public final void mouseReleased(final double mouseX, final double mouseY, final int clickType, final @NonNull InternalContext context) {
		this.dragging = false;
	}

	/* [ Setter Section ] */
	public final <T extends ScrollbarNode> @NonNull T scrollNode(final @NonNull Node scrollNode) {
		this.scrollNode = scrollNode;
		return (T) this;
	}

	/* [ Getter Section ] */
	public final double getScrollHeight() {
		return this.scroll.getHeight() - super.getHeight();
	}

	public final double getScrollWidth() {
		return this.scroll.getWidth() - super.getWidth();
	}

	/* [ Abstract Section ] */
	public abstract void drawScrollbar(final double mouseX, final double mouseY);

	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class ScrollbarBounds {

		private final double minX;
		private final double minY;
		private final double maxX;
		private final double maxY;

		public static @NonNull ScrollbarBounds create(final double minX, final double minY, final double width, final double height) {
			return new ScrollbarBounds(minX, minY, minX + width, minY + height);
		}

		public final double getWidth() {
			return this.maxX - this.minX;
		}

		public final double getHeight() {
			return this.maxY - this.minY;
		}

	}

}