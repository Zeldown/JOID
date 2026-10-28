package com.example.describing.flex;

import com.example.ExampleFont;
import com.example.capture.CaptureStyle;

import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class FlexGrowUI extends UI {

	private final IntegerSignal count = new IntegerSignal(2);

	@Override
	public void init() {
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 24F).color(CaptureStyle.ON_ACCENT);
		final FlexNode list = FlexNode
		.vertical(20, 20, 320)
		.margin(10D)
		.body(flex -> {
			for (int i = 0; i < 4; i++) {
				final int index = i;
				RectNode.create(0, 0, 320, 50).color(CaptureStyle.ACCENT).visible(() -> this.count.get() > index).attach(flex);
			}
		});

		RectNode
		.create(560, 340, 800, 360)
		.color(CaptureStyle.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
		.body(card -> {
			RectNode
			.create(40, 40, 360, 0)
			.color(CaptureStyle.STROKE)
			.height(() -> list.getHeight() + 40D)
			.body(panel -> list.attach(panel))
			.attach(card);
			RectNode
			.create(480, 40, 260, 70)
			.color(CaptureStyle.ACCENT)
			.label(Text.create("Add a bar", label))
			.onClick(event -> this.count.increment())
			.attach(card);
			RectNode
			.create(480, 130, 260, 70)
			.color(CaptureStyle.ACCENT)
			.label(Text.create("Remove a bar", label))
			.onClick(event -> this.count.decrement())
			.attach(card);
		})
		.attach(this);
	}

}