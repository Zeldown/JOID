package com.example.catalog;

import com.example.ExampleFont;
import com.example.capture.CaptureStyle;

import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataPopup;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIDataPopup(active = true)
public class PopupCatalogUI extends UI {

	@Override
	public void init() {
		RectNode
		.create(800, 430, 320, 220)
		.color(CaptureStyle.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
		.body(rect -> TextNode.create(0, 0, 320, 220).text(Text.create("Popup", TextInfo.create(ExampleFont.REGULAR, 30F).color(CaptureStyle.TEXT)).align(Align.CENTER)).attach(rect))
		.attach(this);
	}

}