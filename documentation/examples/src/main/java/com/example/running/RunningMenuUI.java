package com.example.running;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class RunningMenuUI extends UI {

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(Color.decode("#E7E9EE"));
		final BooleanSignal playing = new BooleanSignal(false);

		RectNode.create(0, 0, 1920, 1080).color(Color.decode("#0E1014")).attach(this);

		RectNode
		.create(860, 500, 200, 80)
		.color(Color.decode("#262B35"))
		.onClick(event -> playing.set(true))
		.body(rect -> {
			TextNode
			.create(0, 0, 200, 80)
			.text(() -> Text.create(playing.get() ? "Playing" : "Play", info).align(Align.CENTER))
			.attach(rect);
		})
		.attach(this);
	}

}