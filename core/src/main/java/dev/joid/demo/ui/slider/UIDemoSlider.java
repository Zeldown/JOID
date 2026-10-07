package dev.joid.demo.ui.slider;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.slider.node.DemoIntegerSliderNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.draw.text.builder.modifier.TextModifier;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;

public class UIDemoSlider extends UIDemo {

	private static final Color INK = new Color(153, 153, 153);

	@Override
	public void init() {
		final IntegerSignal value = IntegerSignal.of(3);

		TextNode
		.create(1920 / 2, 1080 / 2 - 70)
		.text(Text.create("value: " + value.get(), TextInfo.create(DemoFont.MONTSERRAT, 25).color(UIDemoSlider.INK), Align.CENTER).modifier(TextModifier.UPPER_CASE))
		.anchorX(Align.CENTER)
		.attach(this);

		DemoIntegerSliderNode
		.create(1920 / 2 - 200, 1080 / 2 - 25, 400, 50)
		.values(1, 9, 3)
		.onChange((node, newValue) -> System.out.println("[UIDemoSlider] slider value: " + newValue))
		.signal(value)
		.attach(this);
	}

}