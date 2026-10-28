package com.example.interactivity.fields;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.control.field.MultilineTextFieldNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class NotesFieldUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(NotesFieldUI.TEXT);

		RectNode
		.create(660, 340, 600, 400)
		.color(NotesFieldUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(NotesFieldUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region notes
			MultilineTextFieldNode
			.create(60, 60, 480, 280)
			.info(info)
			.placeholder("Notes")
			.margin(12D)
			.background(RectNode.create(0, 0, 0, 0).color(NotesFieldUI.ELEMENT))
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}