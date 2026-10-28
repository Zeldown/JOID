package com.example.further;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class FontsUI extends UI {

	private static final Color TEXT = new Color(231, 233, 238).lock();

	// #region load
	private static final FontsPixelFont PIXEL = FontsPixelFont.load("/fonts/Pixel/pixel.png");
	// #endregion

	@Override
	public void init() {
		// #region use
		final TextInfo title = TextInfo.create(FontsUI.PIXEL, 64F).color(FontsUI.TEXT);
		final TextInfo line = TextInfo.create(FontsUI.PIXEL, 24F).color(FontsUI.TEXT);

		TextNode.create(100, 100).text(Text.create("GAME OVER", title)).attach(this);

		TextNode.create(100, 190).text(Text.create("Press any key", line)).attach(this);

		TextNode.create(100, 230).text(Text.create("Synthetic bold", line.withWeight(FontWeight.BOLD))).attach(this);
		// #endregion
	}

}