package com.example.interactivity.keyboard;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class WalkUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	private final DoubleSignal walkX = new DoubleSignal(260D);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(WalkUI.INK);

		RectNode
		.create(660, 390, 600, 300)
		.color(WalkUI.PLACEHOLDER)
		.body(card -> {
			// #region walk
			RectNode
			.create(0, 60, 80, 80)
			.color(WalkUI.INK)
			.x(this.walkX)
			.onUpdate(event -> {
				final double direction = (Key.D.isPhysicalDown() ? 1D : 0D) - (Key.A.isPhysicalDown() ? 1D : 0D);
				this.walkX.set(Math.max(40D, Math.min(480D, this.walkX.peek() + direction * 300D * super.getFrameTime() / 1000D)));
			})
			.attach(card);
			// #endregion
			TextNode.create(40, 220).text(Text.create("Hold A or D", info)).attach(card);
		})
		.attach(this);
	}

}