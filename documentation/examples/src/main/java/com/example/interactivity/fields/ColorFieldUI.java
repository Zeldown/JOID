package com.example.interactivity.fields;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class ColorFieldUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	private final Signal<Color> accent = new Signal<>(new Color(91, 140, 255));

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(ColorFieldUI.TEXT);

		RectNode
		.create(660, 390, 600, 300)
		.color(ColorFieldUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(ColorFieldUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region use
			HexColorFieldNode.create(60, 60, 300, 60).value(this.accent).info(info).attach(card);
			RectNode.create(400, 60, 140, 60).color(this.accent).attach(card);
			// #endregion
		})
		.attach(this);
	}

}