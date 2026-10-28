package com.example.describing.canvas;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.example.ExampleFont;
import com.example.capture.CaptureStyle;
import com.example.capture.ICaptureReadout;

import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.data.UIDataOverlay;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.utils.align.Align;

@UIDataOverlay(active = true)
@UIData(anchorX = Align.START)
public class ScaleUI extends UI implements ICaptureReadout {

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 48F).color(CaptureStyle.TEXT);

		RectNode
		.create(40, 420, 400, 240)
		.color(CaptureStyle.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(CaptureStyle.STROKE, 1.5F))
		.label(Text.create("Default", info))
		.attach(this);
	}

	@Override
	public String getReadout() {
		return "interface scale " + BigDecimal.valueOf(super.getView().getInterfaceScale()).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
	}

}