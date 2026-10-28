package com.example.style;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.data.UIDataPopup;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

@UIData(background = true)
@UIDataPopup(active = true)
public class PopPopupUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(PopPopupUI.TEXT);

		RectNode
		.create(710, 390, 500, 300)
		.color(PopPopupUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(PopPopupUI.ELEMENT, 1.5F))
		.label(Text.create("Saved", info))
		.attach(this);
	}

}