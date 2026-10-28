package com.example.interactivity.drag;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.node.property.DraggableProperty;

public class DragUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(DragUI.TEXT);
		final IntegerSignal drops = new IntegerSignal(0);

		RectNode
		.create(660, 340, 600, 400)
		.color(DragUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(DragUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region drag
			RectNode
			.create(40, 40, 80, 80)
			.color(DragUI.ACCENT)
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