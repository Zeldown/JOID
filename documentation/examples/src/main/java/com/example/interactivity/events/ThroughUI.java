package com.example.interactivity.events;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class ThroughUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(ThroughUI.TEXT);
		final IntegerSignal blocked = new IntegerSignal(0);
		final IntegerSignal through = new IntegerSignal(0);

		RectNode
		.create(520, 390, 880, 300)
		.color(ThroughUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(ThroughUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region blocked
			RectNode
			.create(100, 80, 200, 60)
			.color(ThroughUI.ELEMENT)
			.onClick(event -> blocked.increment())
			.label(Text.create("Order", info))
			.attach(card);
			CircleNode.create(270, 60, 40).color(ThroughUI.ACCENT).attach(card);
			// #endregion
			TextNode.create(200, 200).text(Text.create("Clicks: " + blocked.get(), info)).anchorX(Align.CENTER).attach(card);
			// #region through
			RectNode
			.create(580, 80, 200, 60)
			.color(ThroughUI.ELEMENT)
			.onClick(event -> through.increment())
			.label(Text.create("Order", info))
			.attach(card);
			CircleNode.create(750, 60, 40).color(ThroughUI.ACCENT).interactive(false).attach(card);
			// #endregion
			TextNode.create(680, 200).text(Text.create("Clicks: " + through.get(), info)).anchorX(Align.CENTER).attach(card);
		})
		.attach(this);
	}

}