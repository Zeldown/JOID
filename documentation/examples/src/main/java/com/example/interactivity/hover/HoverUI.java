package com.example.interactivity.hover;

import com.example.ExampleFont;

import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class HoverUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24).color(HoverUI.INK);
		final TextInfo white = TextInfo.create(ExampleFont.REGULAR, 24).color(Color.WHITE);

		RectNode
		.create(660, 390, 600, 300)
		.color(HoverUI.PLACEHOLDER)
		.body(card -> {
			// #region color
			RectNode
			.create(60, 100, 220, 60)
			.color(Color.WHITE)
			.hoveredColor(HoverUI.INK)
			.hoverDuration(300L)
			.hoverEquation(TweenEquations.QUAD_OUT)
			.cursor(Cursor.POINTER)
			.label(Text.create("Shop", info))
			.attach(card);
			// #endregion
			// #region value
			final RectNode link = RectNode
			.create(320, 100, 220, 60)
			.color(HoverUI.INK)
			.label(Text.create("Settings", white))
			.attach(card);
			RectNode
			.create(320, 170, 0, 4)
			.color(HoverUI.INK)
			.width(() -> (double) link.hoverValue(220F))
			.interactive(false)
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}