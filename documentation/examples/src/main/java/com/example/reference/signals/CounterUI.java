package com.example.reference.signals;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

// #region counter
@UIData(background = true, backgroundColor = "#DDDDDD")
public class CounterUI extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	private final IntegerSignal clicks = new IntegerSignal();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28).color(CounterUI.INK);

		TextNode.create(80, 80).text(Text.create("Clicks: " + this.clicks.get(), info)).attach(this);

		RectNode.create(80, 140, 200, 60).color(Color.WHITE).onClick(event -> this.clicks.increment()).attach(this);
	}

}
// #endregion