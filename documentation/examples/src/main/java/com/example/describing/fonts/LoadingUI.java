package com.example.describing.fonts;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class LoadingUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	@Override
	public void init() {
		// #region async
		final RectNode card = RectNode
		.create(660, 440, 600, 200)
		.color(LoadingUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(LoadingUI.ELEMENT, 1.5F))
		.attach(this);
		MsdfFontLoader
		.load("fonts/Montserrat/Montserrat-Regular.ttf")
		.thenAccept(font -> {
			TextNode
			.create(50, 70)
			.text(Text.create("Font ready", TextInfo.create(font, 40F).color(LoadingUI.TEXT)))
			.attach(card);
		});
		// #endregion
	}

}