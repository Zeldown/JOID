package com.example.describing.uis;

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

// #region class
public class MenuUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	@Override
	public void init() {
		final TextInfo title = TextInfo.create(ExampleFont.REGULAR, 40F).color(MenuUI.TEXT);

		RectNode
		.create(760, 340, 400, 400)
		.color(MenuUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(MenuUI.ELEMENT, 1.5F))
		.body(card -> {
			TextNode.create(200, 70).text(Text.create("Menu", title)).anchor(Align.CENTER).attach(card);
			RectNode.create(50, 130, 300, 70).color(MenuUI.ELEMENT).attach(card);
			RectNode.create(50, 220, 300, 70).color(MenuUI.ELEMENT).attach(card);
			RectNode.create(50, 310, 300, 70).color(MenuUI.ELEMENT).attach(card);
		})
		.attach(this);
	}

}
// #endregion