package com.example.interactivity.drag;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.node.property.DraggableProperty;

public class SnapUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(SnapUI.TEXT);
		final StringSignal slot = new StringSignal("none");

		RectNode
		.create(560, 340, 800, 400)
		.color(SnapUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(SnapUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region snap
			final RectNode left = RectNode
			.create(80, 180, 120, 120)
			.color(SnapUI.ELEMENT)
			.label(Text.create("A", info))
			.attach(card);
			final RectNode middle = RectNode
			.create(340, 180, 120, 120)
			.color(SnapUI.ELEMENT)
			.label(Text.create("B", info))
			.attach(card);
			final RectNode right = RectNode
			.create(600, 180, 120, 120)
			.color(SnapUI.ELEMENT)
			.label(Text.create("C", info))
			.attach(card);

			RectNode
			.create(350, 40, 100, 100)
			.color(SnapUI.ACCENT)
			.draggable(DraggableProperty.parent().snap(left, middle, right))
			.onDragSnap(event -> slot.set(event.getTarget().getLabel().getText()))
			.attach(card);
			// #endregion
			TextNode.create(80, 340).text(Text.create("Slot: " + slot.get(), info)).attach(card);
		})
		.attach(this);
	}

}