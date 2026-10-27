package com.example.interactivity.signals;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class CounterUI extends UI {

	private static final Color DARK        = new Color(85, 85, 85).lock();
	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	// #region signal
	private final IntegerSignal clicks = new IntegerSignal(0);
	// #endregion

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(CounterUI.INK);

		RectNode
		.create(660, 340, 600, 400)
		.color(CounterUI.PLACEHOLDER)
		.body(card -> {
			// #region update
			RectNode
			.create(60, 60, 160, 60)
			.color(Color.WHITE)
			.onClick(event -> this.clicks.increment())
			.label(Text.create("+1", info))
			.attach(card);
			// #endregion
			// #region follow
			TextNode
			.create(60, 160)
			.text(Text.create("Clicks: " + this.clicks.get(), info))
			.attach(card);
			RectNode
			.create(60, 220, 0, 40)
			.color(this.clicks.get() >= 3 ? CounterUI.DARK : CounterUI.INK)
			.width(40D + this.clicks.get() * 80D)
			.attach(card);
			// #endregion
			// #region peek
			TextNode
			.create(60, 300)
			.text(Text.create("At open: " + this.clicks.peek(), info))
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}