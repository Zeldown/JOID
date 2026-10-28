package com.example.interactivity.events;

import com.example.ExampleFont;
import com.example.capture.CaptureStyle;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class DispatchDiagramUI extends UI {

	private static final String[] STEPS = {
		"Button: its handleMousePressed listeners",
		"Button: onClick runs and consumes the press",
		"Button: its MONITOR listeners",
		"Card: handleMousePressed listeners skipped",
		"Card: its own handling sees it consumed",
		"Card: its MONITOR listeners",
		"UI: only its MONITOR listeners",
		"The UIs below never receive the press"
	};

	@Override
	public void init() {
		final TextInfo title = TextInfo.create(ExampleFont.REGULAR, 30F).color(CaptureStyle.TEXT);
		final TextInfo small = TextInfo.create(ExampleFont.REGULAR, 22F).color(CaptureStyle.MUTED);
		final TextInfo dark = TextInfo.create(ExampleFont.REGULAR, 24F).color(CaptureStyle.ON_ACCENT);
		final TextInfo light = TextInfo.create(ExampleFont.REGULAR, 24F).color(CaptureStyle.TEXT);
		final TextInfo step = TextInfo.create(ExampleFont.REGULAR, 24F).color(CaptureStyle.TEXT);
		final TextInfo accent = TextInfo.create(ExampleFont.REGULAR, 24F).color(CaptureStyle.ACCENT);

		RectNode
		.create(360, 170, 1200, 740)
		.color(Color.TRANSPARENT)
		.body(backdrop -> {
			TextNode.create(40, 40).text(Text.create("A press on the button", title)).attach(backdrop);
			RectNode
			.create(40, 110, 380, 360)
			.color(CaptureStyle.SURFACE)
			.effect(RoundedNodeEffect.create(12F))
			.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
			.body(card -> {
				TextNode.create(30, 25).text(Text.create("Card", title)).attach(card);
				TextNode.create(30, 70).text(Text.create("handleMousePressed", small)).attach(card);
				RectNode
				.create(30, 150, 320, 180)
				.color(CaptureStyle.ACCENT)
				.effect(RoundedNodeEffect.create(8F))
				.body(button -> {
					TextNode.create(30, 25).text(Text.create("Button", dark)).attach(button);
					TextNode.create(30, 70).text(Text.create("onClick", dark)).attach(button);
				})
				.attach(card);
			})
			.attach(backdrop);
			for (int i = 0; i < DispatchDiagramUI.STEPS.length; i++) {
				final boolean highlighted = i == 1;
				CircleNode
				.create(470, 110 + i * 76, 44)
				.color(highlighted ? CaptureStyle.ACCENT : CaptureStyle.STROKE)
				.label(Text.create(String.valueOf(i + 1), highlighted ? dark : light))
				.attach(backdrop);
				TextNode.create(535, 118 + i * 76).text(Text.create(DispatchDiagramUI.STEPS[i], highlighted ? accent : step)).attach(backdrop);
			}
		})
		.attach(this);
	}

}