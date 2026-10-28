package com.example.interactivity.drag;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.DraggableProperty;

public class HandleUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(HandleUI.TEXT);

		RectNode
		.create(560, 340, 800, 400)
		.color(HandleUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(HandleUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region handle
			final RectNode handle = RectNode.create(0, 0, 300, 40).color(HandleUI.ACCENT).cursor(Cursor.MOVE);

			RectNode
			.create(60, 60, 300, 200)
			.color(HandleUI.ELEMENT)
			.label(Text.create("Window", info))
			.draggable(DraggableProperty.parent().enabled(node -> handle.isHovered()))
			.body(window -> handle.attach(window))
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}