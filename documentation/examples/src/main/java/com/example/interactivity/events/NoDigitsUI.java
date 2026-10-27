package com.example.interactivity.events;

import com.example.ExampleFont;
import com.example.kit.KitTextFieldNode;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class NoDigitsUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24).color(NoDigitsUI.INK);

		RectNode
		.create(760, 440, 400, 200)
		.color(NoDigitsUI.PLACEHOLDER)
		.body(card -> {
			// #region consume
			KitTextFieldNode
			.create(50, 70, 300, 60)
			.info(info)
			.placeholder("No digits")
			.handleCharTyped(event -> {
				if (Character.isDigit(event.getCodepoint())) {
					event.consume();
				}
			})
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}