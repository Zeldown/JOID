package com.example.reference.signals;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

// #region counter
public class CounterUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();

	private final IntegerSignal clicks = new IntegerSignal();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(CounterUI.TEXT);

		TextNode.create(80, 80).text(Text.create("Clicks: " + this.clicks.get(), info)).attach(this);

		RectNode
		.create(80, 140, 200, 60)
		.color(CounterUI.ELEMENT)
		.onClick(event -> this.clicks.increment())
		.attach(this);
	}

}
// #endregion