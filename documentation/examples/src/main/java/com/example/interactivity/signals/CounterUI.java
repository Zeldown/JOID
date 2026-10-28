package com.example.interactivity.signals;

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

public class CounterUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	// #region signal
	private final IntegerSignal clicks = new IntegerSignal(0);
	// #endregion

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(CounterUI.TEXT);

		RectNode
		.create(660, 340, 600, 400)
		.color(CounterUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CounterUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region update
			RectNode
			.create(60, 60, 160, 60)
			.color(CounterUI.ELEMENT)
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
			.color(this.clicks.get() >= 3 ? CounterUI.ACCENT : CounterUI.ELEMENT)
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