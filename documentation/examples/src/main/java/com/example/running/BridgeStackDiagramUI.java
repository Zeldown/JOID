package com.example.running;

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

public class BridgeStackDiagramUI extends UI {

	@Override
	public void init() {
		final TextInfo name = TextInfo.create(ExampleFont.REGULAR, 28F).color(CaptureStyle.TEXT);
		final TextInfo note = TextInfo.create(ExampleFont.REGULAR, 22F).color(CaptureStyle.MUTED);

		RectNode
		.create(660, 290, 600, 140)
		.color(CaptureStyle.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
		.body(rect -> {
			TextNode.create(0, 30, 600, 40).text(Text.create("PanelUIBridge", name).align(Align.CENTER)).attach(rect);
			TextNode.create(0, 80, 600, 30).text(Text.create("priority 10", note).align(Align.CENTER)).attach(rect);
		})
		.attach(this);

		RectNode
		.create(660, 470, 600, 140)
		.color(CaptureStyle.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
		.body(rect -> {
			TextNode.create(0, 30, 600, 40).text(Text.create("Your bridge", name).align(Align.CENTER)).attach(rect);
			TextNode.create(0, 80, 600, 30).text(Text.create("priority 0", note).align(Align.CENTER)).attach(rect);
		})
		.attach(this);

		RectNode
		.create(660, 650, 600, 140)
		.color(CaptureStyle.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
		.body(rect -> {
			TextNode.create(0, 30, 600, 40).text(Text.create("WindowUIBridge", name).align(Align.CENTER)).attach(rect);
			TextNode.create(0, 80, 600, 30).text(Text.create("default, clears the window", note).align(Align.CENTER)).attach(rect);
		})
		.attach(this);

		ContainerNode
		.create(360, 240, 1200, 600)
		.layer((node, mouseX, mouseY) -> {
			DrawUtils.SHAPE.drawLine(CaptureStyle.ACCENT, 4F, Point.of(200D, 60D), Point.of(200D, 530D));
			DrawUtils.SHAPE.drawPolygon(CaptureStyle.ACCENT, Point.of(186D, 520D), Point.of(214D, 520D), Point.of(200D, 548D));
			DrawUtils.SHAPE.drawLine(CaptureStyle.ACCENT, 4F, Point.of(1000D, 540D), Point.of(1000D, 70D));
			DrawUtils.SHAPE.drawPolygon(CaptureStyle.ACCENT, Point.of(986D, 80D), Point.of(1014D, 80D), Point.of(1000D, 52D));
		})
		.attach(this);

		TextNode.create(380, 560, 200, 40).text(Text.create("Input", name).align(Align.CENTER)).attach(this);

		TextNode.create(1340, 560, 200, 40).text(Text.create("Drawing", name).align(Align.CENTER)).attach(this);
	}

}