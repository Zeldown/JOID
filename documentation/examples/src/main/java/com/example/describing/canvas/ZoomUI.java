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
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 96F).color(ZoomUI.TEXT);
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 120F).color(ZoomUI.ON_ACCENT);

		// #region zoom
		super.zoom(0.8D);

		RectNode.create(0, 0, 1920, 1080).color(ZoomUI.SURFACE).attach(this);

		RectNode
		.create(660, 290, 600, 500)
		.color(ZoomUI.ELEMENT)
		.effect(RoundedNodeEffect.create(12F))
		.body(card -> {
			TextNode
			.create(300, 150)
			.text(Text.create(() -> Math.round(super.getZoom() * 100D) + " %", info))
			.anchor(Align.CENTER)
			.attach(card);
			RectNode
			.create(60, 300, 220, 150)
			.color(ZoomUI.ACCENT)
			.label(Text.create("-", label))
			.onClick(event -> super.zoomBy(-0.1D))
			.attach(card);
			RectNode
			.create(320, 300, 220, 150)
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