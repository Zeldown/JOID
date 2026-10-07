package dev.joid.lib.ui.node.impl.structure.slider;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
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
	public void draw(final double mouseX, final double mouseY) {
		if (this.dragging) {
			final double newX = Math.min(Math.max(super.getDefaultX(), mouseX - super.getAbsoluteDefaultX() + super.getDefaultX() - super.dw(2D)), super.getDefaultX() + this.slider.getWidth() - super.getWidth());
			super.x(newX);
		}

		this.drawCursor(mouseX, mouseY);
	}

	public abstract void drawCursor(final double mouseX, final double mouseY);

	@Override
	public void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		if (context.isCancelled() || !this.isHovered(mouseX, mouseY)) {
			return;
		}

		context.cancel(() -> this.dragging = true);
	}

	@Override
	public void mouseReleased(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		this.dragging = false;
	}

	@Override
	public void detach() {
		this.dragging = false;
	}

	@Override
	public boolean isHovered(final double mouseX, final double mouseY, final boolean checkEnabled) {
		return this.dragging || super.isHovered(mouseX, mouseY, checkEnabled);
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