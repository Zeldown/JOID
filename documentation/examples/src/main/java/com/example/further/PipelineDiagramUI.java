package com.example.further;

import com.example.ExampleFont;
import com.example.capture.CaptureStyle;

import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;

public class PipelineDiagramUI extends UI {

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 22F).color(CaptureStyle.TEXT);
		final String[] boxes = {"Your drawing", "Buffer A", "Buffer B", "Buffer A", "Screen"};
		final String[] arrows = {"draws into", "pass 1", "pass 2", "last pass"};

		ContainerNode
		.create(80, 80, 1340, 160)
		.body(container -> {
			for (int i = 0; i < boxes.length; i++) {
				RectNode.create(i * 284, 60, 204, 80).color(CaptureStyle.SURFACE).effect(RoundedNodeEffect.create(12F)).effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F)).label(Text.create(boxes[i], info)).attach(container);
			}

			for (int i = 0; i < arrows.length; i++) {
				TextNode.create(i * 284 + 244, 20).text(Text.create(arrows[i], info)).anchorX(Align.CENTER).attach(container);
			}
		})
		.layer((node, mouseX, mouseY) -> {
			for (int i = 0; i < arrows.length; i++) {
				final double start = i * 284D + 208D;
				final double end = i * 284D + 278D;
				DrawUtils.SHAPE.drawLine(CaptureStyle.ACCENT, 3F, Point.of(start, 100D), Point.of(end, 100D));
				final Point tip = Point.of(end, 100D);
				DrawUtils.SHAPE.drawPolygon(CaptureStyle.ACCENT, tip, Point.of(end - 12D, 92D), Point.of(end - 12D, 108D));
			}
		})
		.attach(this);
	}

}