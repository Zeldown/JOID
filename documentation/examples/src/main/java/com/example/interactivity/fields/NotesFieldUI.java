package com.example.interactivity.fields;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.control.field.MultilineTextFieldNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class NotesFieldUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(NotesFieldUI.INK);

		RectNode
		.create(660, 340, 600, 400)
		.color(NotesFieldUI.PLACEHOLDER)
		.body(card -> {
			// #region notes
			MultilineTextFieldNode
			.create(60, 60, 480, 280)
			.info(info)
			.placeholder("Notes")
			.margin(12D)
			.background(RectNode.create(0, 0, 0, 0).color(Color.WHITE))
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}