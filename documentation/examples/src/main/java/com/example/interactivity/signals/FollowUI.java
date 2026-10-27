package com.example.interactivity.signals;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class FollowUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	private final IntegerSignal clicks = new IntegerSignal(0);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(FollowUI.INK);

		RectNode
		.create(660, 340, 600, 400)
		.color(FollowUI.PLACEHOLDER)
		.body(card -> {
			RectNode
			.create(60, 40, 160, 60)
			.color(Color.WHITE)
			.onClick(event -> this.clicks.increment())
			.label(Text.create("+1", info))
			.attach(card);
			// #region loop
			for (int row = 0; row < 3; row++) {
				final double offset = 40D + row * 60D;
				RectNode
				.create(60, 140 + row * 70, 0, 40)
				.color(FollowUI.INK)
				.width(this.clicks.map(count -> offset + count * 40D))
				.attach(card);
			}
			// #endregion
		})
		.attach(this);
	}

}