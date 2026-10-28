package com.example.tutorial.menu;

import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class ChallengeUI extends UI {

	@Override
	public void init() {
		final TextInfo label = TextInfo.create(Theme.FONT, 36F).color(Theme.TEXT);

		RectNode.create(160, 620, 360, 80).color(Theme.ELEMENT).label(Text.create("Quit", label)).attach(this);

		// #region solution
		RectNode
		.create(160, 720, 360, 80)
		.color(Theme.ELEMENT)
		.label(Text.create("Credits", label))
		.attach(this);
		// #endregion
	}

}