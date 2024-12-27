package be.zeldown.joid.lib.ui.node.impl.structure.slider;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class SliderCursorNode extends Node {

	private boolean dragging;
	private SliderNode<?> slider;

	protected SliderCursorNode(final double width, final double height) {
		super(0, 0, width, height);
	}

	@Override
	public final void draw(final double mouseX, final double mouseY) {
		if (this.dragging) {
			final double newX = Math.min(Math.max(super.getDefaultX(), mouseX - super.getAbsoluteDefaultX() + super.getDefaultX() - super.dw(2D)), super.getDefaultX() + this.slider.getWidth() - super.getWidth());
			super.x(newX);
		}

		this.drawCursor(mouseX, mouseY);
	}

	public abstract void drawCursor(final double mouseX, final double mouseY);

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

	public final <T extends SliderCursorNode> @NonNull T dragging(final boolean dragging) {
		this.dragging = dragging;
		return (T) this;
	}

	public final <T extends SliderCursorNode> @NonNull T slider(final @NonNull SliderNode<?> slider) {
		this.slider = slider;
		return (T) this;
	}

}