package dev.joid.lib.ui.node.impl.structure.slider;

import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class SliderThumbNode extends Node {

	private boolean dragging;
	private SliderNode<?> slider;

	protected SliderThumbNode(final double width, final double height) {
		super(0, 0, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.dragging) {
			final double newX = Math.min(Math.max(super.getDefaultX(), mouseX - super.getAbsoluteDefaultX() + super.getDefaultX() - super.dw(2D)), super.getDefaultX() + this.slider.getWidth() - super.getWidth());
			super.x(newX);
		}

		this.drawThumb(mouseX, mouseY);
	}

	public abstract void drawThumb(final double mouseX, final double mouseY);

	@Override
	public void mousePressed(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {
		if (context.isCancelled() || !this.dragging && !super.isHovered()) {
			return;
		}

		context.cancel(() -> this.dragging = true);
	}

	@Override
	public void mouseReleased(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {
		if (!this.dragging) {
			return;
		}

		this.dragging = false;
		this.slider.release();
	}

	@Override
	public void detach() {
		this.dragging = false;
	}

	@Override
	protected boolean computeHovered() {
		return this.dragging || super.computeHovered();
	}

	public final <T extends SliderThumbNode> @NonNull T dragging(final boolean dragging) {
		this.dragging = dragging;
		return (T) this;
	}

	public final <T extends SliderThumbNode> @NonNull T slider(final @NonNull SliderNode<?> slider) {
		this.slider = slider;
		return (T) this;
	}

}