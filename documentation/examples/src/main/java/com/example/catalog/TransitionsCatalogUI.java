package com.example.catalog;

import com.example.ExampleFont;
import com.example.capture.CaptureStyle;

import dev.joid.lib.JOID;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class TransitionsCatalogUI extends CatalogUI {

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 22F).color(CaptureStyle.ON_ACCENT);

		RectNode
		.create(790, 390, 340, 300)
		.color(CatalogUI.PLACEHOLDER)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CatalogUI.ELEMENT, 1.5F))
		.body(card -> RectNode
		.create(110, 125, 120, 50)
		.color(CatalogUI.INK)
		.onClick(event -> JOID.open(new PopupCatalogUI()))
		.label(Text.create("Open", info))
		.attach(card))
		.attach(this);
	}

}