package com.example.interactivity.drag;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.node.property.DraggableProperty;

public class SnapUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(SnapUI.INK);
		final StringSignal slot = new StringSignal("none");

		RectNode
		.create(560, 340, 800, 400)
		.color(SnapUI.PLACEHOLDER)
		.body(card -> {
			// #region snap
			final RectNode left = RectNode
			.create(80, 180, 120, 120)
			.color(Color.WHITE)
			.label(Text.create("A", info))
			.attach(card);
			final RectNode middle = RectNode
			.create(340, 180, 120, 120)
			.color(Color.WHITE)
			.label(Text.create("B", info))
			.attach(card);
			final RectNode right = RectNode
			.create(600, 180, 120, 120)
			.color(Color.WHITE)
			.label(Text.create("C", info))
			.attach(card);

			RectNode
			.create(350, 40, 100, 100)
			.color(SnapUI.INK)
			.draggable(DraggableProperty.parent().snap(left, middle, right))
			.onDragSnap(event -> slot.set(event.getTarget().getLabel().getText()))
			.attach(card);
			// #endregion
			TextNode.create(80, 340).text(Text.create("Slot: " + slot.get(), info)).attach(card);
		})
		.attach(this);
	}

}