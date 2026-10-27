package com.example.interactivity.fields;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class ColorFieldUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	private final Signal<Color> accent = new Signal<>(new Color(85, 85, 85));

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(ColorFieldUI.INK);

		RectNode
		.create(660, 390, 600, 300)
		.color(ColorFieldUI.PLACEHOLDER)
		.body(card -> {
			// #region use
			HexColorFieldNode.create(60, 60, 300, 60).value(this.accent).info(info).attach(card);
			RectNode.create(400, 60, 140, 60).color(this.accent).attach(card);
			// #endregion
		})
		.attach(this);
	}

}