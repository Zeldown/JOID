package com.example.start;

import com.example.ExampleFont;
import com.example.capture.CaptureStyle;

import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class DevSceneUI extends UI {

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(CaptureStyle.TEXT);

		RectNode
		.create(760, 330, 200, 240)
		.color(CaptureStyle.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
		.<RectNode>onClick(event -> event.getNode().color(CaptureStyle.STROKE))
		.body(card -> {
			TextNode.create(20, 20).text(Text.create("Profile", info)).attach(card);
			FlexNode
			.vertical(20, 70, 160)
			.margin(10D)
			.body(flex -> {
				RectNode.create(0, 0, 160, 50).color(CaptureStyle.ACCENT).attach(flex);
				RectNode.create(0, 0, 160, 50).color(CaptureStyle.STROKE).attach(flex);
			})
			.attach(card);
		})
		.attach(this);
	}

}