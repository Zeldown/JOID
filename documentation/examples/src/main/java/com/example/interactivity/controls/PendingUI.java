package com.example.interactivity.controls;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.control.checkbox.CheckboxNode;
import dev.joid.lib.ui.node.impl.control.field.IntegerFieldNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class PendingUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	private final IntegerSignal tickets = new IntegerSignal(50);
	private final BooleanSignal limited = new BooleanSignal(false);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(PendingUI.TEXT);

		RectNode
		.create(660, 390, 600, 300)
		.color(PendingUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(PendingUI.ELEMENT, 1.5F))
		.body(card -> {
			TextNode.create(60, 55).text(Text.create("Limit to 10", info)).attach(card);
			// #region pending
			new CheckboxNode(260, 40, 50, 50) {}
			.box(RectNode.create(0, 0, 0, 0).color(PendingUI.ELEMENT))
			.mark(RectNode.create(0, 0, 25, 25).color(PendingUI.ACCENT))
			.checked(this.limited)
			.attach(card);
			IntegerFieldNode
			.create(60, 130, 200, 60)
			.max(this.limited.get() ? 10 : 100)
			.value(this.tickets)
			.info(info)
			.background(RectNode.create(0, 0, 0, 0).color(PendingUI.ELEMENT))
			.marginHorizontal(12D)
			.attach(card);
			// #endregion
			TextNode.create(300, 145).text(Text.create("Tickets: " + this.tickets.get(), info)).attach(card);
		})
		.attach(this);
	}

}