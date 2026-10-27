package com.example.interactivity.drag;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.node.property.DraggableProperty;

public class DragUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24).color(DragUI.INK);
		final IntegerSignal drops = new IntegerSignal(0);

		RectNode
		.create(660, 340, 600, 400)
		.color(DragUI.PLACEHOLDER)
		.body(card -> {
			// #region drag
			RectNode
			.create(40, 40, 80, 80)
			.color(DragUI.INK)
			.draggable(DraggableProperty.parent())
			// #region drop
			.onDragDrop(event -> drops.increment())
			// #endregion
			.attach(card);
			// #endregion
			TextNode.create(40, 340).text(Text.create("Drops: " + drops.get(), info)).attach(card);
		})
		.attach(this);
	}

}