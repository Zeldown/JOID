package com.example.interactivity.fields;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class NameFieldUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	// #region signal
	private final StringSignal  name    = new StringSignal("");
	private final IntegerSignal submits = new IntegerSignal(0);
	// #endregion

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(NameFieldUI.TEXT);

		RectNode
		.create(660, 390, 600, 300)
		.color(NameFieldUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(NameFieldUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region field
			TextFieldNode
			.create(60, 60, 480, 60)
			.onSubmit(event -> this.submits.increment())
			.info(info)
			.placeholder("Your name")
			.text(this.name)
			.background(RectNode.create(0, 0, 0, 0).color(NameFieldUI.ELEMENT))
			.marginHorizontal(12D)
			.attach(card);
			// #endregion
			TextNode.create(60, 170).text(Text.create("Hello, " + this.name.get(), info)).attach(card);
			TextNode.create(60, 220).text(Text.create("Submits: " + this.submits.get(), info)).attach(card);
		})
		.attach(this);
	}

}