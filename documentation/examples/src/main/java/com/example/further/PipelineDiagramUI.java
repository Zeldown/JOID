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
		.create(770, 100, 380, 630)
		.body(container -> {
			for (int i = 0; i < boxes.length; i++) {
				RectNode.create(40, 40 + i * 120, 260, 70).color(CaptureStyle.SURFACE).effect(RoundedNodeEffect.create(12F)).effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F)).label(Text.create(boxes[i], info)).attach(container);
			}

			for (int i = 0; i < arrows.length; i++) {
				TextNode.create(200, i * 120 + 135).text(Text.create(arrows[i], info)).anchorY(Align.CENTER).attach(container);
			}
		})
		.layer((node, mouseX, mouseY) -> {
			for (int i = 0; i < arrows.length; i++) {
				final double start = i * 120D + 114D;
				final double end = i * 120D + 156D;
				DrawUtils.SHAPE.drawLine(CaptureStyle.ACCENT, 3F, Point.of(170D, start), Point.of(170D, end));
				final Point tip = Point.of(170D, end);
				DrawUtils.SHAPE.drawPolygon(CaptureStyle.ACCENT, tip, Point.of(162D, end - 12D), Point.of(178D, end - 12D));
			}
		})
		.attach(this);
	}

}