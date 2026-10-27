package com.example.tutorial.menu;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class MenuUI extends UI {

	@Override
	public void init() {
		final TextInfo title = TextInfo.create(Theme.FONT, 120F).weight(FontWeight.BOLD).color(Theme.INK);
		// #region styles
		final TextInfo tagline = TextInfo.create(Theme.FONT, 36F).color(Theme.INK);
		final TextInfo label = TextInfo.create(Theme.FONT, 36F).color(Color.WHITE);
		// #endregion

		// #region title
		TextNode.create(160, 160).text(Text.create("Starfall", title)).attach(this);

		TextNode.create(164, 310).text(Text.create("Catch the falling stars", tagline)).attach(this);
		// #endregion

		// #region buttons
		// #region play
		RectNode
		.create(160, 420, 360, 80)
		.color(Theme.INK)
		.label(Text.create("Play", label))
		.attach(this);
		// #endregion

		RectNode
		.create(160, 520, 360, 80)
		.color(Theme.INK)
		.label(Text.create("Settings", label))
		.attach(this);

		RectNode
		.create(160, 620, 360, 80)
		.color(Theme.INK)
		.label(Text.create("Quit", label))
		.attach(this);
		// #endregion
	}

}