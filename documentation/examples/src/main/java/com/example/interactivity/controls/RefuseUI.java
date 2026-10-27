package com.example.interactivity.controls;

import com.example.ExampleFont;
import com.example.kit.KitIntegerSliderNode;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class RefuseUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	private final IntegerSignal volume = new IntegerSignal(5);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(RefuseUI.INK);

		RectNode
		.create(660, 390, 600, 300)
		.color(RefuseUI.PLACEHOLDER)
		.body(card -> {
			// #region refuse
			KitIntegerSliderNode
			.create(60, 60, 480, 40)
			.range(0, 10)
			.value(this.volume)
			.beforeChange(event -> {
				if (event.getValue() > 8) {
					event.cancel();
				}
			})
			.attach(card);
			RectNode
			.create(60, 150, 200, 60)
			.color(Color.WHITE)
			.onClick(event -> this.volume.set(10))
			.label(Text.create("Set to 10", info))
			.attach(card);
			// #endregion
			TextNode.create(300, 165).text(Text.create("Volume: " + this.volume.get(), info)).attach(card);
		})
		.attach(this);
	}

}