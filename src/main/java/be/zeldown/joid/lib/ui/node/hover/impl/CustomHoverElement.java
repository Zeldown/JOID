package be.zeldown.joid.lib.ui.node.hover.impl;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.hover.HoverElement;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
		default:
			break;
		}

		if (x != null && y != null) {
			GL11.glTranslated(x, y, 0);
		}
		this.element.render(node, mouseX, mouseY);
		if (x != null && y != null) {
			GL11.glTranslated(-x, -y, 0);
		}
	}

	public enum HoverElementPosition {

		FOLLOW,
		FIXED,
		RELATIVE,

	}

}