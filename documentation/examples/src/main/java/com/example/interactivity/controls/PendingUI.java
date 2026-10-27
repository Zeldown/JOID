package com.example.interactivity.controls;

import com.example.ExampleFont;
import com.example.kit.KitCheckboxNode;
import com.example.kit.KitIntegerFieldNode;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class PendingUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	private final IntegerSignal tickets = new IntegerSignal(50);
	private final BooleanSignal limited = new BooleanSignal(false);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24).color(PendingUI.INK);

		RectNode
		.create(660, 390, 600, 300)
		.color(PendingUI.PLACEHOLDER)
		.body(card -> {
			TextNode.create(60, 55).text(Text.create("Limit to 10", info)).attach(card);
			// #region pending
			KitCheckboxNode.create(260, 40, 50).checked(this.limited).attach(card);
			KitIntegerFieldNode
			.create(60, 130, 200, 60)
			.max(this.limited.get() ? 10 : 100)
			.value(this.tickets)
			.info(info)
			.attach(card);
			// #endregion
			TextNode.create(300, 145).text(Text.create("Tickets: " + this.tickets.get(), info)).attach(card);
		})
		.attach(this);
	}

}