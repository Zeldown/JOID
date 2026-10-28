package com.example.describing.text;

import com.example.ExampleFont;
import com.example.capture.CaptureStyle;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.draw.text.TextMode;
import dev.joid.lib.draw.text.TextOverflow;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class ModesFlowUI extends UI {

	private final long start = BridgeHandler.CLOCK.get().currentTimeMillis();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(CaptureStyle.TEXT);
		final TextInfo caption = TextInfo.create(ExampleFont.REGULAR, 20F).color(CaptureStyle.MUTED);
		final String sentence = "A sentence too long for the width of its box, in four modes.";

		RectNode
		.create(460, 290, 1000, 500)
		.color(CaptureStyle.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
		.body(card -> {
			RectNode.create(40, 60, 440, 40).color(CaptureStyle.STROKE).width(() -> this.getBoxWidth()).attach(card);
			RectNode.create(520, 60, 440, 40).color(CaptureStyle.STROKE).width(() -> this.getBoxWidth()).attach(card);
			RectNode.create(40, 220, 440, 40).color(CaptureStyle.STROKE).width(() -> this.getBoxWidth()).attach(card);
			RectNode.create(40, 380, 440, 40).color(CaptureStyle.STROKE).width(() -> this.getBoxWidth()).attach(card);
			TextNode
			.create(40, 60, 440, 40)
			.text(Text.create(sentence, info).overflow(TextOverflow.ELLIPSIS))
			.mode(TextMode.OVERFLOW)
			.width(() -> this.getBoxWidth())
			.attach(card);
			TextNode.create(520, 60, 440, 40).text(Text.create(sentence, info)).mode(TextMode.SPLIT).width(() -> this.getBoxWidth()).attach(card);
			TextNode.create(40, 220, 440, 40).text(Text.create(sentence, info)).mode(TextMode.BOX).width(() -> this.getBoxWidth()).attach(card);
			TextNode.create(40, 380, 440, 40).text(Text.create(sentence, info)).width(() -> this.getBoxWidth()).attach(card);
			TextNode.create(40, 20).text(Text.create("OVERFLOW", caption)).attach(card);
			TextNode.create(520, 20).text(Text.create("SPLIT", caption)).attach(card);
			TextNode.create(40, 180).text(Text.create("BOX", caption)).attach(card);
			TextNode.create(40, 340).text(Text.create("NORMAL", caption)).attach(card);
		})
		.attach(this);
	}

	private double getBoxWidth() {
		final double time = (BridgeHandler.CLOCK.get().currentTimeMillis() - this.start) / 4000D;
		return 340D + 100D * Math.cos(time * 2D * Math.PI);
	}

}