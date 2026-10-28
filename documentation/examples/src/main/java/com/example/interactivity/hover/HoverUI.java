package com.example.interactivity.hover;

import com.example.ExampleFont;

import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class HoverUI extends UI {

	private static final Color HOVER     = new Color(54, 61, 77).lock();
	private static final Color TEXT      = new Color(231, 233, 238).lock();
	private static final Color ELEMENT   = new Color(38, 43, 53).lock();
	private static final Color SURFACE   = new Color(23, 26, 33).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(HoverUI.TEXT);
		final TextInfo dark = TextInfo.create(ExampleFont.REGULAR, 24F).color(HoverUI.ON_ACCENT);

		RectNode
		.create(660, 390, 600, 300)
		.color(HoverUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(HoverUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region color
			RectNode
			.create(60, 100, 220, 60)
			.color(HoverUI.ELEMENT)
			.hoveredColor(HoverUI.HOVER)
			.hoverDuration(300L)
			.hoverEquation(TweenEquations.QUAD_OUT)
			.cursor(Cursor.POINTER)
			.label(Text.create("Shop", info))
			.attach(card);
			// #endregion
			// #region value
			final RectNode link = RectNode
			.create(320, 100, 220, 60)
			.color(HoverUI.ACCENT)
			.label(Text.create("Settings", dark))
			.attach(card);
			RectNode
			.create(320, 170, 0, 4)
			.color(HoverUI.ACCENT)
			.width(() -> (double) link.hoverValue(220F))
			.interactive(false)
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}