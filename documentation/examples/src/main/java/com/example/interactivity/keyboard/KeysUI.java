package com.example.interactivity.keyboard;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class KeysUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	private final StringSignal typed = new StringSignal("");
	private final StringSignal key = new StringSignal("none");

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(KeysUI.INK);

		// #region keys
		super.handleKeyPressed(event -> this.key.set(event.getKey().name()));
		super.handleCharTyped(event -> this.typed.append(new String(Character.toChars(event.getCodepoint()))));
		// #endregion

		RectNode
		.create(660, 440, 600, 200)
		.color(KeysUI.PLACEHOLDER)
		.body(card -> {
			TextNode.create(60, 50).text(Text.create("Key: " + this.key.get(), info)).attach(card);
			TextNode.create(60, 110).text(Text.create("Typed: " + this.typed.get(), info)).attach(card);
		})
		.attach(this);
	}

}