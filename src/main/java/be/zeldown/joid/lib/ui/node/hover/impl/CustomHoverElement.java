package be.zeldown.joid.lib.ui.node.hover.impl;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.hover.HoverElement;
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

		Double x = null;
		Double y = null;

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
			x = 0D;
			y = 0D;
			break;
		default:
			break;
		}

		if (x == null || y == null) {
			return;
		}

		x += this.element.getX();
		y += this.element.getY();
		y -= this.element.getHeight();

		final UI ui = node.getUi();
		final double elementWidth = this.element.getWidth();
		final double elementHeight = this.element.getHeight();

		if (ui != null && elementWidth > 0 && elementHeight > 0) {
			final double absoluteX = ui.getAbsoluteX(x);
			final double absoluteY = ui.getAbsoluteY(y);
			final double absoluteWidth = ui.getAbsoluteWidth(elementWidth);

			final double screenWidth = Display.getWidth();

			if (absoluteX + absoluteWidth > screenWidth) {
				x -= absoluteX + absoluteWidth - screenWidth;
			}

			if (absoluteY < 0) {
				y -= absoluteY;
			}
		}

		GL11.glTranslated(x, y, 0);
		this.element.render(node, mouseX, mouseY);
		GL11.glTranslated(-x, -y, 0);
	}

	public enum HoverElementPosition {

		FOLLOW,
		FIXED,
		RELATIVE,

	}

}