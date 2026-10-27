package com.example.interactivity.fields;

import com.example.ExampleFont;
import com.example.interactivity.kit.PlainIntegerFieldNode;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class NumberFieldUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	private final IntegerSignal players = new IntegerSignal(4);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24).color(NumberFieldUI.INK);

		RectNode
		.create(660, 390, 600, 300)
		.color(NumberFieldUI.PLACEHOLDER)
		.body(card -> {
			// #region number
			PlainIntegerFieldNode
			.create(60, 60, 240, 60)
			.min(1)
			.max(16)
			.step(1)
			.value(this.players)
			.info(info)
			.attach(card);
			// #endregion
			TextNode.create(60, 170).text(Text.create("Players: " + this.players.get(), info)).attach(card);
		})
		.attach(this);
	}

}