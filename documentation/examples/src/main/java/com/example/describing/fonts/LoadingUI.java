package com.example.describing.fonts;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class LoadingUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		// #region async
		final RectNode card = RectNode.create(660, 440, 600, 200).color(LoadingUI.PLACEHOLDER).attach(this);
		MsdfFontLoader
		.load("fonts/Montserrat/Montserrat-Regular.ttf")
		.thenAccept(font -> TextNode
			.create(50, 70)
			.text(Text.create("Font ready", TextInfo.create(font, 40F).color(LoadingUI.INK)))
			.attach(card));
		// #endregion
	}

}