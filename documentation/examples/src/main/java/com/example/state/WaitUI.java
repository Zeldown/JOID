package com.example.state;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.task.Task;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class WaitUI extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	// #region signal
	private final Signal<String> profile = new Signal<>();
	// #endregion

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(WaitUI.INK);

		// #region load
		Task.create(2000L).onRun(event -> this.profile.set("Ada Lovelace")).attach(this);
		// #endregion

		// #region wait
		RectNode
		.create(660, 390, 600, 300)
		.color(Color.WHITE)
		.wait(card -> this.profile.isPresent())
		.skeleton(card -> RectNode.create(0, 0, card.getWidth(), card.getHeight()).color(WaitUI.INK))
		.body(card -> {
			TextNode.create(40, 40).text(Text.create(this.profile.map(name -> "Name: " + name), info)).attach(card);
		})
		.attach(this);
		// #endregion
	}

}