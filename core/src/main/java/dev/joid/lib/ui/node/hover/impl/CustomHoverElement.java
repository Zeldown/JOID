package dev.joid.lib.ui.node.hover.impl;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.hover.HoverElement;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomHoverElement implements HoverElement {

	private final HoverElement element;
	private final HoverElementPosition position;

	public static @NonNull CustomHoverElement follow(final @NonNull HoverElement element) {
		return new CustomHoverElement(element, HoverElementPosition.FOLLOW);
	}

	public static @NonNull CustomHoverElement fixed(final @NonNull HoverElement element) {
		return new CustomHoverElement(element, HoverElementPosition.FIXED);
	}

	public static @NonNull CustomHoverElement relative(final @NonNull HoverElement element) {
		return new CustomHoverElement(element, HoverElementPosition.RELATIVE);
	}

	@Override
	public void render(final @NonNull Node node, final double mouseX, final double mouseY) {
		if (this.element == null || this.position == null) {
			return;
		}

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
		y -= this.element.getHeight();

		final UI ui = node.getUi();
		final double elementWidth = this.element.getWidth();
		final double elementHeight = this.element.getHeight();

		if (ui != null && elementWidth > 0 && elementHeight > 0) {
			final double right = ui.getView().toUiX(ui.getWidth());
			final double top = ui.getView().toUiY(0D);
			if (x + elementWidth > right) {
				x = right - elementWidth;
			}

			if (y < top) {
				y = top;
			}
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.pushMatrix();
		try {
			render.translate(x, y, 0);
			this.element.render(node, mouseX, mouseY);
		} finally {
			render.popMatrix();
		}
	}

	public enum HoverElementPosition {

		FOLLOW,
		FIXED,
		RELATIVE;

	}

}