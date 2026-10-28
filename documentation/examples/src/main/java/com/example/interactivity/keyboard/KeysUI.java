package com.example.interactivity.keyboard;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class KeysUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	private final StringSignal typed = new StringSignal("");
	private final StringSignal key   = new StringSignal("none");

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(KeysUI.TEXT);

		// #region keys
		super.handleKeyPressed(event -> this.key.set(event.getKey().name()));
		super.handleCharTyped(event -> this.typed.append(new String(Character.toChars(event.getCodepoint()))));
		// #endregion

		RectNode
		.create(660, 440, 600, 200)
		.color(KeysUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(KeysUI.ELEMENT, 1.5F))
		.body(card -> {
			TextNode.create(60, 50).text(Text.create("Key: " + this.key.get(), info)).attach(card);
			TextNode.create(60, 110).text(Text.create("Typed: " + this.typed.get(), info)).attach(card);
		})
		.attach(this);
	}

}