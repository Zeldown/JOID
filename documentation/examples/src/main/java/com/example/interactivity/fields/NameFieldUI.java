package com.example.interactivity.fields;

import com.example.ExampleFont;
import com.example.kit.KitTextFieldNode;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class NameFieldUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	// #region signal
	private final StringSignal name = new StringSignal("");
	private final IntegerSignal submits = new IntegerSignal(0);
	// #endregion

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24).color(NameFieldUI.INK);

		RectNode
		.create(660, 390, 600, 300)
		.color(NameFieldUI.PLACEHOLDER)
		.body(card -> {
			// #region field
			KitTextFieldNode
			.create(60, 60, 480, 60)
			.onSubmit(event -> this.submits.increment())
			.info(info)
			.placeholder("Your name")
			.text(this.name)
			.attach(card);
			// #endregion
			TextNode.create(60, 170).text(Text.create("Hello, " + this.name.get(), info)).attach(card);
			TextNode.create(60, 220).text(Text.create("Submits: " + this.submits.get(), info)).attach(card);
		})
		.attach(this);
	}

}