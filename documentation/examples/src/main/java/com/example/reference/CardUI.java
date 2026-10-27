package com.example.reference;

import dev.joid.lib.color.Color;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class CardUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final BooleanSignal selected = new BooleanSignal(false);

		// #region node
		RectNode
		.create(760, 390, 400, 300)
		.color(selected.map(value -> value ? Color.WHITE : CardUI.PLACEHOLDER))
		.body(card -> {
			CircleNode.create(40, 40, 120).color(CardUI.INK).attach(card);
			RectNode.create(190, 70, 170, 30).color(CardUI.INK).attach(card);
		})
		.onClick(event -> selected.toggle())
		.attach(this);
		// #endregion
	}

}