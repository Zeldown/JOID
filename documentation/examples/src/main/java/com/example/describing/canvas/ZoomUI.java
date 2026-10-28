package com.example.describing.canvas;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class ZoomUI extends UI {

	private static final Color TEXT      = new Color(231, 233, 238).lock();
	private static final Color ELEMENT   = new Color(38, 43, 53).lock();
	private static final Color SURFACE   = new Color(23, 26, 33).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 56F).color(ZoomUI.TEXT);
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 56F).color(ZoomUI.ON_ACCENT);

		RectNode.create(0, 0, 1920, 1080).color(ZoomUI.SURFACE).attach(this);

		// #region zoom
		RectNode
		.create(760, 340, 400, 400)
		.color(ZoomUI.ELEMENT)
		.effect(RoundedNodeEffect.create(12F))
		.body(card -> {
			TextNode
			.create(200, 120)
			.text(Text.create(() -> Math.round(super.getZoom() * 100D) + " %", info))
			.anchor(Align.CENTER)
			.attach(card);
			RectNode
			.create(50, 250, 130, 100)
			.color(ZoomUI.ACCENT)
			.label(Text.create("-", label))
			.onClick(event -> super.zoomBy(-0.1D))
			.attach(card);
			RectNode
			.create(220, 250, 130, 100)
			.color(ZoomUI.ACCENT)
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