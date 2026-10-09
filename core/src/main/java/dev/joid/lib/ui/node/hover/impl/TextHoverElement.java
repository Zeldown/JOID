package dev.joid.lib.ui.node.hover.impl;

import java.util.List;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.hover.IHoverElement;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TextHoverElement implements IHoverElement {

	private final List<String> lines;

	@Override
	public void render(final @NonNull Node node, final double mouseX, final double mouseY) {
		final UI ui = node.getUi();
		if (ui == null) {
			return;
		}

		ui.drawHover(this.lines, mouseX, mouseY);
	}

}