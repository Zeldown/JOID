package com.example.running;

import com.example.ExampleFont;
import com.example.capture.CaptureStyle;

import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class RunningPanelUI extends UI {

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(CaptureStyle.TEXT);

		RectNode
		.create(1480, 40, 400, 200)
		.color(CaptureStyle.STROKE)
		.hover(() -> "A panel of the library")
		.body(rect -> TextNode.create(0, 0, 400, 200).text(Text.create("Panel", info).align(Align.CENTER)).attach(rect))
		.attach(this);
	}

}