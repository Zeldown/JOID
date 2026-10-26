package dev.joid.lib.ui.node.hover.impl;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.hover.IHoverElement;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PositionedHoverElement implements IHoverElement {

	private final IHoverElement        element;
	private final HoverElementPosition position;

	@Override
	public void render(final @NonNull Node node, final double mouseX, final double mouseY) {
		double x;
		double y;

		switch (this.position) {
		case FOLLOW:
			x = mouseX;
			y = mouseY;
			break;
		case RELATIVE:
			x = node.getAbsoluteX();
			y = node.getAbsoluteY();
			break;
		case FIXED:
		default:
			x = 0D;
			y = 0D;
			break;
		}

		x += this.element.getX();
		y += this.element.getY();

		final UI ui = node.getUi();
		final double elementWidth = this.element.getWidth();
		final double elementHeight = this.element.getHeight();

		if (elementWidth > 0D && elementHeight > 0D) {
			final double left = ui.getView().toUiX(0D);
			final double top = ui.getView().toUiY(0D);
			final double right = ui.getView().toUiX(ui.getWindowWidth());
			final double bottom = ui.getView().toUiY(ui.getWindowHeight());
			x = Math.max(left, Math.min(x, right - elementWidth));
			y = Math.max(top, Math.min(y, bottom - elementHeight));
		}

		final double elementX = x;
		final double elementY = y;
		BridgeHandler.RENDER.get().getModelView().with(matrix -> {
			matrix.translate(elementX, elementY, 0D);
			this.element.render(node, mouseX, mouseY);
		});
	}

	@Override
	public void detach() {
		this.element.detach();
	}

	public enum HoverElementPosition {

		FOLLOW,
		FIXED,
		RELATIVE;

	}

}