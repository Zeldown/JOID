package com.example.describing.text;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class AlignUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 32F).color(AlignUI.TEXT);

		RectNode
		.create(660, 390, 600, 300)
		.color(AlignUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(AlignUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region align
			RectNode
			.create(150, 50, 300, 80)
			.color(AlignUI.ELEMENT)
			.body(button -> TextNode.create(0, 0, 300, 80).text(Text.create("Play", info).align(Align.CENTER)).attach(button))
			.attach(card);
			// #endregion
			// #region label
			RectNode.create(150, 170, 300, 80).color(AlignUI.ELEMENT).label(Text.create("Settings", info)).attach(card);
			// #endregion
		})
		.attach(this);
	}

}