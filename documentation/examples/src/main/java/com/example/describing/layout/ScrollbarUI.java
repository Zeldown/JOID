package com.example.describing.layout;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.control.scrollbar.ScrollbarNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.OverflowProperty;

public class ScrollbarUI extends UI {

	private static final Color ELEMENT   = new Color(38, 43, 53).lock();
	private static final Color SURFACE   = new Color(23, 26, 33).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 26F).color(ScrollbarUI.ON_ACCENT);

		RectNode
		.create(720, 360, 480, 360)
		.color(ScrollbarUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(ScrollbarUI.ELEMENT, 1.5F))
		.body(card -> {
			RectNode
			.create(20, 20, 440, 320)
			.color(ScrollbarUI.ELEMENT)
			// #region scrollbar
			.overflow(OverflowProperty.SCROLL)
			.scrollbar(new ScrollbarNode(424, 8, 8, 304) {}
			.track(RectNode.create(0, 0, 0, 0).color(ScrollbarUI.SURFACE))
			.thumb(RectNode.create(0, 0, 0, 0).color(ScrollbarUI.ACCENT)))
			// #endregion
			.body(area -> {
				for (int i = 0; i < 8; i++) {
					RectNode
					.create(20, 20 + i * 80, 400, 60)
					.color(ScrollbarUI.ACCENT)
					.label(Text.create(String.valueOf(i + 1), info))
					.attach(area);
				}
			})
			.attach(card);
		})
		.attach(this);
	}

}