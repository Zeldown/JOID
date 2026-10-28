package com.example.describing.canvas;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.example.ExampleFont;
import com.example.capture.CaptureStyle;
import com.example.capture.ICaptureReadout;

import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.view.UIView;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class ConvertSceneUI extends UI implements ICaptureReadout {

	@Override
	public void init() {
		final TextInfo muted = TextInfo.create(ExampleFont.REGULAR, 64F).color(CaptureStyle.MUTED);
		final TextInfo text = TextInfo.create(ExampleFont.REGULAR, 64F).color(CaptureStyle.TEXT);
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 72F).weight(FontWeight.BOLD).color(CaptureStyle.TEXT);
		final TextInfo dark = TextInfo.create(ExampleFont.REGULAR, 64F).weight(FontWeight.BOLD).color(CaptureStyle.ON_ACCENT);
		final Align[] anchors = {Align.START, Align.CENTER, Align.END};

		super.handleKeyPressed(event -> {
			final int index = event.getKey().ordinal() - Key.DIGIT_1.ordinal();
			if (index >= 0 && index < anchors.length) {
				super.getData().anchorX(anchors[index]);
			}
		});

		RectNode
		.create(0, 0, 1920, 1080)
		.color(CaptureStyle.SURFACE)
		.effect(BorderNodeEffect.create(CaptureStyle.LINE, 8F))
		.body(canvas -> {
			TextNode.create(40, 30, 600, 80).text(Text.create("0, 0", muted).align(Align.START)).attach(canvas);
			TextNode.create(1280, 970, 600, 80).text(Text.create("1920, 1080", muted).alignX(Align.END).alignY(Align.START)).attach(canvas);
			TextNode.create(960, 250, 520, 100).text(Text.create("1560, 300", label).alignX(Align.END).alignY(Align.CENTER)).attach(canvas);
		})
		.attach(this);

		CircleNode
		.create(0, 0, 48)
		.color(CaptureStyle.MUTED)
		.x(() -> super.getData().getAnchorPositionX() - 24D)
		.y(() -> super.getData().getAnchorPositionY() - 24D)
		.interactive(false)
		.attach(this);

		TextNode
		.create(0, 0, 400, 80)
		.text(Text.create("anchor", muted).align(Align.CENTER))
		.x(() -> super.getData().getAnchorPositionX() * (1D - 400D / 1920D))
		.y(() -> super.getData().getAnchorPositionY() + 40D)
		.interactive(false)
		.attach(this);

		CircleNode.create(1524, 264, 72).color(CaptureStyle.ACCENT).interactive(false).attach(this);

		for (int index = 0; index < anchors.length; index++) {
			final Align anchor = anchors[index];
			final String name = (index + 1) + " " + anchor.name();
			RectNode
			.create(180 + index * 540, 830, 480, 150)
			.color(() -> super.getData().anchorX() == anchor ? CaptureStyle.ACCENT : CaptureStyle.STROKE)
			.effect(RoundedNodeEffect.create(20F))
			.label(() -> Text.create(name, super.getData().anchorX() == anchor ? dark : text))
			.interactive(false)
			.attach(this);
		}
	}

	@Override
	public String getReadout() {
		final UIView view = super.getView();
		final String zoom = BigDecimal.valueOf(super.getZoom()).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
		return "anchor " + super.getData().anchorX() + " · zoom " + zoom + " · (1560, 300) → (" + Math.round(view.toScreenX(1560D)) + ", " + Math.round(view.toScreenY(300D)) + ")";
	}

}