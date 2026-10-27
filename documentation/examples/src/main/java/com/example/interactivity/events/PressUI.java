package com.example.interactivity.events;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class PressUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24).color(PressUI.INK);
		final IntegerSignal button = new IntegerSignal(0);
		final IntegerSignal heard = new IntegerSignal(0);
		final IntegerSignal hovered = new IntegerSignal(0);
		final IntegerSignal monitored = new IntegerSignal(0);

		// #region listen
		RectNode
		.create(760, 340, 400, 400)
		.color(PressUI.PLACEHOLDER)
		.handleMousePressed(event -> {
			heard.increment();
			if (event.getNode().isHovered()) {
				hovered.increment();
			}
		})
		.handleMousePressed(EventListener.priority(EventPriority.MONITOR, event -> monitored.increment()))
		.body(card -> {
			RectNode
			.create(100, 60, 200, 60)
			.color(Color.WHITE)
			.onClick(event -> button.increment())
			.label(Text.create("Button", info))
			.attach(card);
			TextNode.create(60, 170).text(Text.create("Button: " + button.get(), info)).attach(card);
			TextNode.create(60, 220).text(Text.create("Card heard: " + heard.get(), info)).attach(card);
			TextNode.create(60, 270).text(Text.create("Card hovered: " + hovered.get(), info)).attach(card);
			TextNode.create(60, 320).text(Text.create("Monitor: " + monitored.get(), info)).attach(card);
		})
		.attach(this);
		// #endregion
	}

}