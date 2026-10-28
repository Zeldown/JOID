package com.example.running;

import com.example.ExampleFont;
import com.example.capture.CaptureStyle;

import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class RunningStartUI extends UI {

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(CaptureStyle.TEXT);

		RectNode
		.create(680, 390, 560, 300)
		.color(CaptureStyle.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
		.body(card -> {
			RectNode
			.create(180, 110, 200, 80)
			.color(CaptureStyle.STROKE)
			.hover(() -> "Opens the settings")
			.body(rect -> TextNode.create(0, 0, 200, 80).text(Text.create("Settings", info).align(Align.CENTER)).attach(rect))
			.attach(card);
		})
		.attach(this);
	}

}