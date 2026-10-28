package com.example.tutorial.setup;

import dev.joid.lib.JOID;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class ChallengeUI extends UI {

	@Override
	public void init() {
		final TextInfo title = TextInfo.create(Theme.FONT, 120F).weight(FontWeight.BOLD).color(Theme.TEXT);
		// #region style
		final TextInfo small = TextInfo.create(Theme.FONT, 28F).color(Theme.TEXT);
		// #endregion

		TextNode
		.create(960, 540)
		.text(Text.create("Starfall", title))
		.anchor(Align.CENTER)
		.attach(this);

		// #region solution
		TextNode
		.create(960, 640)
		.text(Text.create("JOID " + JOID.VERSION, small))
		.anchor(Align.CENTER)
		.attach(this);
		// #endregion
	}

}