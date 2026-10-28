package com.example.state;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.ComputedSignal;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class DerivedUI extends UI {

	private static final Color TEXT      = new Color(231, 233, 238).lock();
	private static final Color ELEMENT   = new Color(38, 43, 53).lock();
	private static final Color SURFACE   = new Color(23, 26, 33).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(DerivedUI.TEXT);
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 24F).color(DerivedUI.ON_ACCENT);
		// #region sources
		final IntegerSignal price = new IntegerSignal(12);
		final IntegerSignal quantity = new IntegerSignal(1);
		// #endregion
		// #region from
		final ComputedSignal<Integer> total = Signal.from(() -> price.get() * quantity.get());
		// #endregion
		// #region listen
		final IntegerSignal changes = new IntegerSignal(0);
		total.onChange(value -> changes.increment());
		// #endregion

		RectNode
		.create(560, 290, 800, 500)
		.color(DerivedUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(DerivedUI.ELEMENT, 1.5F))
		.body(card -> {
			RectNode.create(60, 60, 200, 60).color(DerivedUI.ACCENT).onClick(event -> price.increment()).label(Text.create("Price +1", label)).attach(card);
			RectNode.create(300, 60, 200, 60).color(DerivedUI.ACCENT).onClick(event -> quantity.increment()).label(Text.create("Quantity +1", label)).attach(card);
			// #region batch
			RectNode
			.create(540, 60, 200, 60)
			.color(DerivedUI.ACCENT)
			.onClick(event -> Signal.batch(() -> {
				price.increment();
				quantity.increment();
			}))
			.label(Text.create("Both +1", label))
			.attach(card);
			// #endregion
			// #region map
			TextNode.create(60, 170).text(Text.create(price.map(value -> "Price: " + value), info)).attach(card);
			// #endregion
			TextNode.create(60, 240).text(Text.create(quantity.map(value -> "Quantity: " + value), info)).attach(card);
			// #region total
			TextNode.create(60, 310).text(Text.create(total.map(value -> "Total: " + value), info)).attach(card);
			// #endregion
			TextNode.create(60, 380).text(Text.create(changes.map(value -> "Changes: " + value), info)).attach(card);
		})
		.attach(this);
	}

}