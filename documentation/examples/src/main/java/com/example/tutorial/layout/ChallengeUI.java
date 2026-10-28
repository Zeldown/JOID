package com.example.tutorial.layout;

import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class ChallengeUI extends UI {

	@Override
	public void init() {
		final TextInfo label = TextInfo.create(Theme.FONT, 36F).color(Theme.TEXT);

		// #region solution
		FlexNode
		.vertical(160, 420, 360)
		.margin(40D)
		.body(flex -> {
			RectNode.create(0, 0, 360, 80).color(Theme.ELEMENT).label(Text.create("Play", label)).attach(flex);
			RectNode.create(0, 0, 360, 80).color(Theme.ELEMENT).label(Text.create("Settings", label)).attach(flex);
			RectNode.create(0, 0, 360, 80).color(Theme.ELEMENT).label(Text.create("Credits", label)).attach(flex);
			RectNode.create(0, 0, 360, 80).color(Theme.ELEMENT).label(Text.create("Quit", label)).attach(flex);
		})
		.attach(this);
		// #endregion
	}

}