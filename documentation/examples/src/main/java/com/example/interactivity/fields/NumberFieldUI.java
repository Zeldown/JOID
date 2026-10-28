package com.example.interactivity.fields;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.control.field.IntegerFieldNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class NumberFieldUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	private final IntegerSignal players = new IntegerSignal(4);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(NumberFieldUI.TEXT);

		RectNode
		.create(660, 390, 600, 300)
		.color(NumberFieldUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(NumberFieldUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region number
			IntegerFieldNode
			.create(60, 60, 240, 60)
			.min(1)
			.max(16)
			.step(1)
			.value(this.players)
			.info(info)
			.background(RectNode.create(0, 0, 0, 0).color(NumberFieldUI.ELEMENT))
			.marginHorizontal(12D)
			.attach(card);
			// #endregion
			TextNode.create(60, 170).text(Text.create("Players: " + this.players.get(), info)).attach(card);
		})
		.attach(this);
	}

}