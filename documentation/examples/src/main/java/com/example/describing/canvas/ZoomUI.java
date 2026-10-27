package com.example.describing.canvas;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class ZoomUI extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 32F).color(ZoomUI.INK);
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 40F).color(Color.WHITE);

		// #region zoom
		RectNode
		.create(760, 340, 400, 400)
		.color(ZoomUI.PLACEHOLDER)
		.body(card -> {
			TextNode
			.create(200, 120)
			.text(Text.create(() -> Math.round(super.getZoom() * 100D) + " %", info))
			.anchor(Align.CENTER)
			.attach(card);
			RectNode
			.create(50, 250, 130, 100)
			.color(ZoomUI.INK)
			.label(Text.create("-", label))
			.onClick(event -> super.zoomBy(-0.1D))
			.attach(card);
			RectNode
			.create(220, 250, 130, 100)
			.color(ZoomUI.INK)
			.label(Text.create("+", label))
			.onClick(event -> super.zoomBy(0.1D))
			.attach(card);
		})
		.attach(this);
		// #endregion

		// #region limit
		super.beforeZoom(event -> {
			if (event.getValue() < 0.5D) {
				event.cancel();
			}
		});
		// #endregion
	}

}