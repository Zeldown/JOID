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

public class FollowUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	private final IntegerSignal clicks = new IntegerSignal(0);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(FollowUI.TEXT);

		RectNode
		.create(660, 340, 600, 400)
		.color(FollowUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(FollowUI.ELEMENT, 1.5F))
		.body(card -> {
			RectNode
			.create(60, 40, 160, 60)
			.color(FollowUI.ELEMENT)
			.onClick(event -> this.clicks.increment())
			.label(Text.create("+1", info))
			.attach(card);
			// #region loop
			for (int row = 0; row < 3; row++) {
				final double offset = 40D + row * 60D;
				RectNode
				.create(60, 140 + row * 70, 0, 40)
				.color(FollowUI.ACCENT)
				.width(this.clicks.map(count -> offset + count * 40D))
				.attach(card);
			}
			// #endregion
		})
		.attach(this);
	}

}