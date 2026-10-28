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
		.create(720, 290, 460, 140)
		.color(CaptureStyle.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
		.body(rect -> {
			TextNode.create(0, 30, 460, 40).text(Text.create("PanelUIBridge", name).align(Align.CENTER)).attach(rect);
			TextNode.create(0, 80, 460, 30).text(Text.create("priority 10", note).align(Align.CENTER)).attach(rect);
		})
		.attach(this);

		RectNode
		.create(720, 470, 460, 140)
		.color(CaptureStyle.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
		.body(rect -> {
			TextNode.create(0, 30, 460, 40).text(Text.create("Your bridge", name).align(Align.CENTER)).attach(rect);
			TextNode.create(0, 80, 460, 30).text(Text.create("priority 0", note).align(Align.CENTER)).attach(rect);
		})
		.attach(this);

		RectNode
		.create(720, 650, 460, 140)
		.color(CaptureStyle.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
		.body(rect -> {
			TextNode.create(0, 30, 460, 40).text(Text.create("WindowUIBridge", name).align(Align.CENTER)).attach(rect);
			TextNode.create(0, 80, 460, 30).text(Text.create("default, clears the window", note).align(Align.CENTER)).attach(rect);
		})
		.attach(this);

		ContainerNode
		.create(540, 240, 840, 600)
		.layer((node, mouseX, mouseY) -> {
			DrawUtils.SHAPE.drawLine(CaptureStyle.ACCENT, 4F, Point.of(150D, 60D), Point.of(150D, 530D));
			DrawUtils.SHAPE.drawPolygon(CaptureStyle.ACCENT, Point.of(136D, 520D), Point.of(164D, 520D), Point.of(150D, 548D));
			DrawUtils.SHAPE.drawLine(CaptureStyle.ACCENT, 4F, Point.of(690D, 540D), Point.of(690D, 70D));
			DrawUtils.SHAPE.drawPolygon(CaptureStyle.ACCENT, Point.of(676D, 80D), Point.of(704D, 80D), Point.of(690D, 52D));
		})
		.attach(this);

		TextNode.create(540, 560, 140, 40).text(Text.create("Input", name).align(Align.CENTER)).attach(this);

		TextNode.create(1240, 560, 140, 40).text(Text.create("Drawing", name).align(Align.CENTER)).attach(this);
	}

}