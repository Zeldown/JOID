package com.example.state;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.task.Task;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class TimerUI extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(TimerUI.INK);
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 24F).color(Color.WHITE);
		// #region every
		final IntegerSignal seconds = new IntegerSignal(0);
		Task.create(1000L).every(1000L).onRun(event -> seconds.increment()).attach(this);
		// #endregion
		// #region once
		final BooleanSignal saved = new BooleanSignal(false);
		final Task hide = Task.create(3000L).onRun(event -> saved.set(false));
		// #endregion

		RectNode
		.create(560, 340, 800, 400)
		.color(Color.WHITE)
		.body(card -> {
			TextNode.create(60, 60).text(Text.create(seconds.map(value -> "Open for " + value + " s"), info)).attach(card);
			// #region click
			RectNode
			.create(60, 160, 260, 60)
			.color(TimerUI.INK)
			.onClick(event -> {
				saved.set(true);
				hide.attach(this);
			})
			.label(Text.create("Save", label))
			.attach(card);
			// #endregion
			TextNode.create(360, 172).text(Text.create("Saved", info)).visible(saved).attach(card);
		})
		.attach(this);
	}

}