package be.zeldown.joid.lib.ui.node.hover.impl;

import java.util.List;

import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.hover.HoverElement;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultHoverElement implements HoverElement {

	private final List<String> lines;

	@Override
	public void render(final @NonNull Node node, final double mouseX, final double mouseY) {
		final UI ui = node.getUi();
		if (ui == null) {
			return;
		}

		ui.getBridge().drawHover(this.lines, mouseX, mouseY);
	}

}