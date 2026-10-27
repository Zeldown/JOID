package com.example.further;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.text.TextNode;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class FontsUI extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	// #region load
	private static final FontsPixelFont PIXEL = FontsPixelFont.load("/fonts/Pixel/pixel.png");
	// #endregion

	@Override
	public void init() {
		// #region use
		final TextInfo title = TextInfo.create(FontsUI.PIXEL, 64F).color(FontsUI.INK);
		final TextInfo line = TextInfo.create(FontsUI.PIXEL, 24F).color(FontsUI.INK);

		TextNode.create(100, 100).text(Text.create("GAME OVER", title)).attach(this);

		TextNode.create(100, 190).text(Text.create("Press any key", line)).attach(this);

		TextNode.create(100, 230).text(Text.create("Synthetic bold", line.withWeight(FontWeight.BOLD))).attach(this);
		// #endregion
	}

}