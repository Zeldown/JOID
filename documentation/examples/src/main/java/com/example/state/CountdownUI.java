package com.example.state;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.task.Task;
import dev.joid.lib.utils.align.Align;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class CountdownUI extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 48F).color(CountdownUI.INK);
		final IntegerSignal left = new IntegerSignal(3);

		// #region countdown
		Task
		.create(1000L)
		.every(1000L)
		.onRun(event -> {
			if (left.decrement().peek() == 0) {
				event.getTask().detach();
			}
		})
		.attach(this);
		// #endregion

		TextNode
		.create(960, 540)
		.text(Text.create(left.map(value -> value > 0 ? "Starts in " + value : "Go"), info))
		.anchor(Align.CENTER)
		.attach(this);
	}

}