package com.example.describing.markup;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class SweepUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 32F).color(SweepUI.TEXT);

		RectNode
		.create(610, 480, 700, 120)
		.color(SweepUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(SweepUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region sweep
			TextNode
			.create(50, 40)
			.text(Text.create("Loading the next level", info.withEffects(SweepTextEffect.inst())))
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}