package com.example.running;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class RunningStartUI extends UI {

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(Color.decode("#E7E9EE"));

		RectNode
		.create(680, 390, 560, 300)
		.color(Color.decode("#171A21"))
		.body(card -> {
			RectNode
			.create(180, 110, 200, 80)
			.color(Color.decode("#262B35"))
			.hover(() -> "Opens the settings")
			.body(rect -> TextNode.create(0, 0, 200, 80).text(Text.create("Settings", info).align(Align.CENTER)).attach(rect))
			.attach(card);
		})
		.attach(this);
	}

}