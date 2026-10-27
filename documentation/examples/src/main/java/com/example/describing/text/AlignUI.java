package com.example.describing.text;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class AlignUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 32F).color(AlignUI.INK);

		RectNode
		.create(660, 390, 600, 300)
		.color(AlignUI.PLACEHOLDER)
		.body(card -> {
			// #region align
			RectNode
			.create(150, 50, 300, 80)
			.color(Color.WHITE)
			.body(button -> TextNode.create(0, 0, 300, 80).text(Text.create("Play", info).align(Align.CENTER)).attach(button))
			.attach(card);
			// #endregion
			// #region label
			RectNode.create(150, 170, 300, 80).color(Color.WHITE).label(Text.create("Settings", info)).attach(card);
			// #endregion
		})
		.attach(this);
	}

}