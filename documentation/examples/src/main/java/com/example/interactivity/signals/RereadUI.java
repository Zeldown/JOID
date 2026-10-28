package com.example.interactivity.signals;

import com.example.ExampleFont;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class RereadUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	private final IntegerSignal clicks = new IntegerSignal(0);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(RereadUI.TEXT);

		RectNode
		.create(560, 340, 800, 400)
		.color(RereadUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(RereadUI.ELEMENT, 1.5F))
		.body(card -> {
			RectNode
			.create(60, 40, 160, 60)
			.color(RereadUI.ELEMENT)
			.onClick(event -> this.clicks.increment())
			.label(Text.create("+1", info))
			.attach(card);
			TextNode.create(60, 145).text(Text.create("Value", info)).attach(card);
			TextNode.create(60, 225).text(Text.create("Expression", info)).attach(card);
			TextNode.create(60, 305).text(Text.create("Lambda", info)).attach(card);
			// #region forms
			RectNode.create(260, 140, 0, 40).color(RereadUI.ACCENT).width(200D).attach(card);
			RectNode.create(260, 220, 0, 40).color(RereadUI.ACCENT).width(40D + this.clicks.get() * 80D).attach(card);
			RectNode
			.create(260, 300, 0, 40)
			.color(RereadUI.ACCENT)
			.width(() -> 40D + BridgeHandler.CLOCK.get().currentTimeMillis() % 2000L / 5D)
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}