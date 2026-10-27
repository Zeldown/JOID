package com.example.tutorial.interaction;

import dev.joid.lib.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class ChallengeUI extends UI {

	@Override
	public void init() {
		final TextInfo label = TextInfo.create(Theme.FONT, 36F).color(Color.WHITE);

		FlexNode
		.vertical(160, 420, 360)
		.margin(20D)
		.body(flex -> {
			// #region solution
			RectNode
			.create(0, 0, 360, 80)
			.color(Theme.INK)
			.hoveredColor(Theme.DARK)
			.onClick(event -> JOID.close(this))
			.hover(() -> "Escape quits too")
			.label(Text.create("Quit", label))
			.attach(flex);
			// #endregion
		})
		.attach(this);
	}

}