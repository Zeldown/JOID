package com.example.interactivity.hover;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.DraggableProperty;

public class CursorUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color MUTED   = new Color(139, 147, 163).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(CursorUI.TEXT);

		RectNode
		.create(660, 390, 600, 300)
		.color(CursorUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CursorUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region cursor
			RectNode
			.create(60, 60, 220, 60)
			.color(CursorUI.ELEMENT)
			.cursor(Cursor.POINTER)
			.label(Text.create("Shop", info))
			.attach(card);
			TextFieldNode
			.create(320, 60, 220, 60)
			.info(info)
			.placeholder("Name")
			.placeholderColor(CursorUI.MUTED)
			.background(RectNode.create(0, 0, 0, 0).color(CursorUI.ELEMENT))
			.marginHorizontal(12D)
			.attach(card);
			RectNode
			.create(60, 180, 480, 60)
			.color(CursorUI.ELEMENT)
			.cursor(Cursor.MOVE)
			.draggable(DraggableProperty.parent())
			.label(Text.create("Drag me", info))
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}