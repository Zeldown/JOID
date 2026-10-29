package com.example.describing.layout;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.OverflowProperty;

public class ScrollUI extends UI {

	private static final Color ELEMENT   = new Color(38, 43, 53).lock();
	private static final Color SURFACE   = new Color(23, 26, 33).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 26F).color(ScrollUI.ON_ACCENT);

		RectNode
		.create(720, 310, 480, 360)
		.color(ScrollUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(ScrollUI.ELEMENT, 1.5F))
		.body(card -> {
			RectNode
			.create(20, 20, 440, 320)
			.color(ScrollUI.ELEMENT)
			.overflow(OverflowProperty.SCROLL)
			.body(area -> {
				for (int i = 0; i < 8; i++) {
					RectNode
					.create(20, 20 + i * 80, 400, 60)
					.color(ScrollUI.ACCENT)
					.label(Text.create(String.valueOf(i + 1), info))
					.attach(area);
				}
			})
			.attach(card);
		})
		.attach(this);
	}

}