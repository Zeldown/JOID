package com.example.interactivity.events;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class LimitUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(LimitUI.INK);
		final IntegerSignal refused = new IntegerSignal(0);

		RectNode
		.create(660, 390, 600, 300)
		.color(LimitUI.PLACEHOLDER)
		.body(card -> {
			// #region before
			final RectNode square = RectNode
			.create(40, 40, 60, 60)
			.color(LimitUI.INK)
			.beforeMove(event -> {
				if (event.getValue().getX() > 400D) {
					refused.increment();
					event.cancel();
				}
			})
			.attach(card);
			RectNode
			.create(40, 200, 200, 60)
			.color(Color.WHITE)
			.onClick(event -> square.move(square.getX() + 100D, 40D))
			.label(Text.create("Move right", info))
			.attach(card);
			// #endregion
			TextNode.create(280, 215).text(Text.create("Refused: " + refused.get(), info)).attach(card);
		})
		.attach(this);
	}

}