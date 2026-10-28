package com.example.depth;

import com.example.ExampleFont;
import com.example.capture.CaptureStyle;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class MsdfZoomUI extends UI {

	private final long start = BridgeHandler.CLOCK.get().currentTimeMillis();

	@Override
	public void init() {
		RectNode
		.create(660, 340, 600, 400)
		.color(CaptureStyle.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
		.body(card -> {
			TextNode
			.create(300, 200)
			.text(() -> Text.create("Ag", TextInfo.create(ExampleFont.REGULAR, this.getSize()).color(CaptureStyle.TEXT)).align(Align.CENTER))
			.anchor(Align.CENTER)
			.attach(card);
		})
		.attach(this);
	}

	private float getSize() {
		final double time = (BridgeHandler.CLOCK.get().currentTimeMillis() - this.start) / 4000D;
		return (float) (122D - 98D * Math.cos(time * 2D * Math.PI));
	}

}