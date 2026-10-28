package com.example.interactivity.events;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class NoDigitsUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color MUTED   = new Color(139, 147, 163).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(NoDigitsUI.TEXT);

		RectNode
		.create(760, 440, 400, 200)
		.color(NoDigitsUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(NoDigitsUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region consume
			TextFieldNode
			.create(50, 70, 300, 60)
			.info(info)
			.placeholder("No digits")
			.placeholderColor(NoDigitsUI.MUTED)
			.background(RectNode.create(0, 0, 0, 0).color(NoDigitsUI.ELEMENT))
			.marginHorizontal(12D)
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