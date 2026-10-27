package com.example.interactivity.events;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class ThroughUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24).color(ThroughUI.INK);
		final IntegerSignal blocked = new IntegerSignal(0);
		final IntegerSignal through = new IntegerSignal(0);

		RectNode
		.create(520, 390, 880, 300)
		.color(ThroughUI.PLACEHOLDER)
		.body(card -> {
			// #region blocked
			RectNode
			.create(100, 80, 200, 60)
			.color(Color.WHITE)
			.onClick(event -> blocked.increment())
			.label(Text.create("Order", info))
			.attach(card);
			CircleNode.create(270, 60, 40).color(ThroughUI.INK).attach(card);
			// #endregion
			TextNode.create(200, 200).text(Text.create("Clicks: " + blocked.get(), info)).anchorX(Align.CENTER).attach(card);
			// #region through
			RectNode
			.create(580, 80, 200, 60)
			.color(Color.WHITE)
			.onClick(event -> through.increment())
			.label(Text.create("Order", info))
			.attach(card);
			CircleNode.create(750, 60, 40).color(ThroughUI.INK).interactive(false).attach(card);
			// #endregion
			TextNode.create(680, 200).text(Text.create("Clicks: " + through.get(), info)).anchorX(Align.CENTER).attach(card);
		})
		.attach(this);
	}

}