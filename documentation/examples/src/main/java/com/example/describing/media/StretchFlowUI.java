package com.example.describing.media;

import com.example.ExampleFont;
import com.example.capture.CaptureStyle;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.resource.ResourceNode.StretchType;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class StretchFlowUI extends UI {

	private final long start = BridgeHandler.CLOCK.get().currentTimeMillis();

	@Override
	public void init() {
		final Resource image = Resource.of("images/placeholder.png");
		final TextInfo caption = TextInfo.create(ExampleFont.REGULAR, 20F).color(CaptureStyle.MUTED);
		final StretchType[] types = {StretchType.STRETCH, StretchType.CONTAIN, StretchType.COVER};

		RectNode
		.create(490, 340, 940, 400)
		.color(CaptureStyle.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
		.body(card -> {
			for (int i = 0; i < types.length; i++) {
				final double center = 170D + i * 300D;
				RectNode
				.create(center, 190, 180, 180)
				.color(CaptureStyle.STROKE)
				.anchor(Align.CENTER)
				.width(() -> this.getBoxWidth())
				.height(() -> this.getBoxHeight())
				.attach(card);
				ResourceNode
				.create(center, 190, 180, 180)
				.resource(image)
				.stretch(types[i])
				.anchor(Align.CENTER)
				.width(() -> this.getBoxWidth())
				.height(() -> this.getBoxHeight())
				.attach(card);
				TextNode.create(center, 350).text(Text.create(types[i].name(), caption)).anchor(Align.CENTER).attach(card);
			}
		})
		.attach(this);
	}

	private double getBoxWidth() {
		return 180D + 80D * this.getStretch();
	}

	private double getBoxHeight() {
		return 180D - 80D * this.getStretch();
	}

	private double getStretch() {
		final double time = (BridgeHandler.CLOCK.get().currentTimeMillis() - this.start) / 3000D;
		return Math.sin(time * 2D * Math.PI);
	}

}