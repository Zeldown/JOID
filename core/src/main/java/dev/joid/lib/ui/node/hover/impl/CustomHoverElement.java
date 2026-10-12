package dev.joid.lib.ui.node.hover.impl;

import dev.joid.lib.ui.node.hover.IHoverElement;
import lombok.NonNull;

public class CustomHoverElement extends PositionedHoverElement {

	protected CustomHoverElement(final IHoverElement element, final HoverElementPosition position) {
		super(element, position);
	}

	public static @NonNull CustomHoverElement follow(final @NonNull IHoverElement element) {
		return new CustomHoverElement(element, HoverElementPosition.FOLLOW);
	}

	public static @NonNull CustomHoverElement fixed(final @NonNull IHoverElement element) {
		return new CustomHoverElement(element, HoverElementPosition.FIXED);
	}

	public static @NonNull CustomHoverElement relative(final @NonNull IHoverElement element) {
		return new CustomHoverElement(element, HoverElementPosition.RELATIVE);
	}

}