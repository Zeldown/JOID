package com.example.tutorial.settings;

import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.hook.property.UIProperty;
import dev.joid.lib.ui.node.impl.text.TextNode;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class ChallengeUI extends UI {

	// #region field
	@UIProperty
	private int launches;
	// #endregion

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(Theme.FONT, 26F).color(Theme.INK);

		// #region solution
		this.launches++;
		TextNode.create(164, 810).text(Text.create("Launch " + this.launches, caption)).attach(this);
		// #endregion
	}

}