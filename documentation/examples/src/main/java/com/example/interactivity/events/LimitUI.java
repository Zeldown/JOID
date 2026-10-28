package com.example.interactivity.events;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class LimitUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(LimitUI.TEXT);
		final IntegerSignal refused = new IntegerSignal(0);

		RectNode
		.create(660, 390, 600, 300)
		.color(LimitUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(LimitUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region before
			final RectNode square = RectNode
			.create(40, 40, 60, 60)
			.color(LimitUI.ACCENT)
			.beforeMove(event -> {
				if (event.getValue().getX() > 400D) {
					refused.increment();
					event.cancel();
				}
			})
			.attach(card);
			RectNode
			.create(40, 200, 200, 60)
			.color(LimitUI.ELEMENT)
			.onClick(event -> square.move(square.getX() + 100D, 40D))
			.label(Text.create("Move right", info))
			.attach(card);
			// #endregion
			TextNode.create(280, 215).text(Text.create("Refused: " + refused.get(), info)).attach(card);
		})
		.attach(this);
	}

}