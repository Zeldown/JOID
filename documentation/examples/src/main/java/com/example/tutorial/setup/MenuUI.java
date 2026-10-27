package com.example.tutorial.setup;

import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class MenuUI extends UI {

	@Override
	public void init() {
		final TextInfo title = TextInfo.create(Theme.FONT, 120F).weight(FontWeight.BOLD).color(Theme.INK);

		TextNode
		.create(960, 540)
		.text(Text.create("Starfall", title))
		.anchor(Align.CENTER)
		.attach(this);
	}

}