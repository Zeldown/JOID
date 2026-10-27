package com.example.interactivity.drag;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.DraggableProperty;

public class HandleUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24).color(HandleUI.INK);

		RectNode
		.create(560, 340, 800, 400)
		.color(HandleUI.PLACEHOLDER)
		.body(card -> {
			// #region handle
			final RectNode handle = RectNode.create(0, 0, 300, 40).color(HandleUI.INK).cursor(Cursor.MOVE);

			RectNode
			.create(60, 60, 300, 200)
			.color(Color.WHITE)
			.label(Text.create("Window", info))
			.draggable(DraggableProperty.parent().enabled(node -> handle.isHovered() || node.isDragging()))
			.body(window -> handle.attach(window))
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}