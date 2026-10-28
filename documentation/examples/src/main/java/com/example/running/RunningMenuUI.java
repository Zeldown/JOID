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

	private static final Color STAGE   = new Color(14, 16, 20).lock();
	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(RunningMenuUI.TEXT);
		final BooleanSignal playing = new BooleanSignal(false);

		RectNode.create(0, 0, 1920, 1080).color(RunningMenuUI.STAGE).attach(this);

		RectNode
		.create(860, 500, 200, 80)
		.color(RunningMenuUI.ELEMENT)
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