package com.example.describing.uis;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

// #region class
public class MenuUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo title = TextInfo.create(ExampleFont.REGULAR, 40F).color(MenuUI.INK);

		RectNode
		.create(760, 340, 400, 400)
		.color(MenuUI.PLACEHOLDER)
		.body(card -> {
			TextNode.create(200, 70).text(Text.create("Menu", title)).anchor(Align.CENTER).attach(card);
			RectNode.create(50, 130, 300, 70).color(Color.WHITE).attach(card);
			RectNode.create(50, 220, 300, 70).color(Color.WHITE).attach(card);
			RectNode.create(50, 310, 300, 70).color(Color.WHITE).attach(card);
		})
		.attach(this);
	}

}
// #endregion