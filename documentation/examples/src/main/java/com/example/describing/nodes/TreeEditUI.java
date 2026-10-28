package com.example.describing.nodes;

import com.example.ExampleFont;
import com.example.capture.CaptureStyle;

import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class TreeEditUI extends UI {

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(CaptureStyle.TEXT);

		RectNode
		.create(660, 290, 600, 400)
		.color(CaptureStyle.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
		.body(card -> {
			final RectNode row = RectNode.create(20, 20, 560, 100).color(CaptureStyle.STROKE).attach(card);
			RectNode
			.create(20, 140, 160, 60)
			.color(CaptureStyle.STROKE)
			.label(Text.create("Add", info))
			.onClick(event -> RectNode.create(20 + row.getChildren().size() * 80, 20, 60, 60).color(CaptureStyle.ACCENT).attach(row))
			.attach(card);
			RectNode
			.create(200, 140, 160, 60)
			.color(CaptureStyle.STROKE)
			.label(Text.create("Remove", info))
			.onClick(event -> {
				if (!row.getChildren().isEmpty()) {
					row.remove(row.getChild(row.getChildren().size() - 1, Node.class));
				}
			})
			.attach(card);
			RectNode.create(20, 240, 240, 100).color(CaptureStyle.ACCENT).onClick(event -> event.getNode().attach(card)).attach(card);
			RectNode.create(120, 270, 240, 100).color(CaptureStyle.MUTED).onClick(event -> event.getNode().attach(card)).attach(card);
		})
		.attach(this);
	}

}