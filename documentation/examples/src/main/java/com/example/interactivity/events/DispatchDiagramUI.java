package com.example.interactivity.events;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class DispatchDiagramUI extends UI {

	private static final Color LABEL    = new Color(85, 85, 85).lock();
	private static final Color INK      = new Color(153, 153, 153).lock();
	private static final Color ACCENT   = new Color(124, 92, 255).lock();
	private static final Color BACKDROP = new Color(221, 221, 221).lock();

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
		final TextInfo title = TextInfo.create(ExampleFont.REGULAR, 30F).color(DispatchDiagramUI.LABEL);
		final TextInfo small = TextInfo.create(ExampleFont.REGULAR, 22F).color(DispatchDiagramUI.INK);
		final TextInfo white = TextInfo.create(ExampleFont.REGULAR, 24F).color(Color.WHITE);
		final TextInfo step = TextInfo.create(ExampleFont.REGULAR, 24F).color(DispatchDiagramUI.LABEL);
		final TextInfo accent = TextInfo.create(ExampleFont.REGULAR, 24F).color(DispatchDiagramUI.ACCENT);

		RectNode
		.create(360, 170, 1200, 740)
		.color(DispatchDiagramUI.BACKDROP)
		.body(backdrop -> {
			TextNode.create(40, 40).text(Text.create("A press on the button", title)).attach(backdrop);
			RectNode
			.create(40, 110, 380, 360)
			.color(Color.WHITE)
			.body(card -> {
				TextNode.create(30, 25).text(Text.create("Card", title)).attach(card);
				TextNode.create(30, 70).text(Text.create("handleMousePressed", small)).attach(card);
				RectNode
				.create(30, 150, 320, 180)
				.color(DispatchDiagramUI.ACCENT)
				.body(button -> {
					TextNode.create(30, 25).text(Text.create("Button", white)).attach(button);
					TextNode.create(30, 70).text(Text.create("onClick", white)).attach(button);
				})
				.attach(card);
			})
			.attach(backdrop);
			TextNode.create(40, 510).text(Text.create("The front child receives the press first,", small)).attach(backdrop);
			TextNode.create(40, 545).text(Text.create("then its parent, up to the UI.", small)).attach(backdrop);
			for (int i = 0; i < DispatchDiagramUI.STEPS.length; i++) {
				final boolean highlighted = i == 1;
				CircleNode
				.create(470, 110 + i * 76, 44)
				.color(highlighted ? DispatchDiagramUI.ACCENT : DispatchDiagramUI.LABEL)
				.label(Text.create(String.valueOf(i + 1), white))
				.attach(backdrop);
				TextNode.create(535, 118 + i * 76).text(Text.create(DispatchDiagramUI.STEPS[i], highlighted ? accent : step)).attach(backdrop);
			}
		})
		.attach(this);
	}

}