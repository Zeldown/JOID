package com.example.interactivity.events;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class PressUI extends UI {

	private static final Color TEXT      = new Color(231, 233, 238).lock();
	private static final Color ELEMENT   = new Color(38, 43, 53).lock();
	private static final Color SURFACE   = new Color(23, 26, 33).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(PressUI.TEXT);
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 24F).color(PressUI.ON_ACCENT);
		final IntegerSignal button = new IntegerSignal(0);
		final IntegerSignal heard = new IntegerSignal(0);
		final IntegerSignal hovered = new IntegerSignal(0);
		final IntegerSignal monitored = new IntegerSignal(0);

		// #region listen
		RectNode
		.create(760, 340, 400, 400)
		.color(PressUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(PressUI.ELEMENT, 1.5F))
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
			.color(PressUI.ACCENT)
			.onClick(event -> button.increment())
			.label(Text.create("Button", label))
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