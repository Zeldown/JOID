package dev.joid.lib.ui.node.impl.structure.slider;

import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class SliderThumbNode extends Node {

	private boolean dragging;
	private SliderNode<?> slider;

	protected SliderThumbNode(final double width, final double height) {
		super(0, 0, width, height);
		super.<SliderThumbNode>require("slider", "pass it to slider.thumb(...) instead of attaching it", node -> node.slider != null);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.dragging) {
			final double newX = Math.min(Math.max(super.getDefaultX(), mouseX - super.getAbsoluteDefaultX() + super.getDefaultX() - super.dw(2D)), super.getDefaultX() + this.slider.getWidth() - super.getWidth());
			super.layoutPosition(this, newX - super.getDefaultX(), super.getLayoutY());
		}

		this.drawThumb(mouseX, mouseY);
	}

	public abstract void drawThumb(final double mouseX, final double mouseY);

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.isConsumed() || !event.getButton().isLeft() || !this.dragging && !super.isHovered()) {
			return;
		}

		this.dragging = true;
		event.consume();
	}

	@Override
	public void mouseReleased(final @NonNull NodeMouseEvent<?> event) {
		if (!this.dragging || !event.getButton().isLeft()) {
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

	protected final <T extends SliderThumbNode> @NonNull T dragging(final boolean dragging) {
		this.dragging = dragging;
		return (T) this;
	}

	public final <T extends SliderThumbNode> @NonNull T slider(final @NonNull SliderNode<?> slider) {
		this.slider = slider;
		return (T) this;
	}

}