package dev.joid.lib.ui.node.hover.impl;

import java.util.List;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.hover.IHoverElement;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class TextHoverElement implements IHoverElement {

	private final List<String> lines;

	public static @NonNull TextHoverElement create(final @NonNull List<@NonNull String> lines) {
		return new TextHoverElement(lines);
	}

	@Override
	public void render(final @NonNull Node node, final double mouseX, final double mouseY) {
		node.getUi().drawHover(this.lines, mouseX, mouseY);
	}

}