package com.example.interactivity.keyboard;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.shortcut.Shortcut;

public class ShortcutUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color MUTED   = new Color(139, 147, 163).lock();

	private final IntegerSignal saves = new IntegerSignal(0);
	private final IntegerSignal helps = new IntegerSignal(0);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(ShortcutUI.TEXT);

		// #region shortcut
		Shortcut.create(Key.S).ctrl(true).onTrigger(event -> this.saves.increment()).attach(this);
		Shortcut.create(Key.F1).overrideFocus(true).onTrigger(event -> this.helps.increment()).attach(this);
		// #endregion

		RectNode
		.create(660, 390, 600, 300)
		.color(ShortcutUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(ShortcutUI.ELEMENT, 1.5F))
		.body(card -> {
			TextFieldNode
			.create(60, 40, 480, 60)
			.info(info)
			.placeholder("Notes")
			.placeholderColor(ShortcutUI.MUTED)
			.background(RectNode.create(0, 0, 0, 0).color(ShortcutUI.ELEMENT))
			.marginHorizontal(12D)
			.attach(card);
			TextNode.create(60, 150).text(Text.create("Ctrl + S: " + this.saves.get(), info)).attach(card);
			TextNode.create(60, 200).text(Text.create("F1: " + this.helps.get(), info)).attach(card);
		})
		.attach(this);
	}

}