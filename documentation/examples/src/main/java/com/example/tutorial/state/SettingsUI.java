package com.example.tutorial.state;

import dev.joid.lib.JOID;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.data.UIDataPopup;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

@UIDataPopup(active = true)
@UIData(background = true, backgroundColor = "#00000080")
public class SettingsUI extends UI {

	@Override
	public void init() {
		final TextInfo heading = TextInfo.create(Theme.FONT, 40F).weight(FontWeight.BOLD).color(Theme.TEXT);
		final TextInfo caption = TextInfo.create(Theme.FONT, 30F).color(Theme.TEXT);
		final TextInfo label = TextInfo.create(Theme.FONT, 30F).color(Theme.TEXT);

		RectNode
		.create(660, 290, 600, 500)
		.color(Theme.SURFACE)
		.body(card -> {
			TextNode.create(48, 40).text(Text.create("Settings", heading)).attach(card);
			TextNode.create(48, 150).text(Text.create("Music: on", caption)).attach(card);
			TextNode.create(48, 260).text(Text.create("Volume: 80 %", caption)).attach(card);
			RectNode
			.create(48, 380, 504, 80)
			.color(Theme.ELEMENT)
			.hoveredColor(Theme.HOVER)
			.onClick(event -> JOID.close(this))
			.label(Text.create("Close", label))
			.attach(card);
		})
		.attach(this);
	}

}