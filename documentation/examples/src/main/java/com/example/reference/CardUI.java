package com.example.reference;

import dev.joid.lib.color.Color;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class CardUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	@Override
	public void init() {
		final BooleanSignal selected = new BooleanSignal(false);

		// #region node
		RectNode
		.create(760, 390, 400, 300)
		.color(selected.map(value -> value ? CardUI.ELEMENT : CardUI.SURFACE))
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CardUI.ELEMENT, 1.5F))
		.body(card -> {
			CircleNode.create(40, 40, 120).color(CardUI.ACCENT).attach(card);
			RectNode.create(190, 70, 170, 30).color(CardUI.ACCENT).attach(card);
		})
		.onClick(event -> selected.toggle())
		.attach(this);
		// #endregion
	}

}