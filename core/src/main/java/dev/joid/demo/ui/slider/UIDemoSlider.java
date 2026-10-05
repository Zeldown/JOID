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

	@Override
	public void init() {
		final IntegerSignal valueSignal = new IntegerSignal(3);

		TextNode
		.create(1920 / 2, 1080 / 2 - 70)
		.text(Text.create("", TextInfo.create(DemoFont.MONTSERRAT, 25).color(Color.WHITE), Align.CENTER).modifier(TextModifier.UPPER_CASE))
		.<TextNode>onInit(node -> node.getText().text("value: " + valueSignal.getOrDefault()))
		.watch(valueSignal)
		.anchorX(Align.CENTER)
		.attach(this);

		DemoIntegerSliderNode
		.create(1920 / 2 - 200, 1080 / 2 - 25, 400, 50)
		.values(1, 9, valueSignal.getOrDefault())
		.onChange((node, value) -> System.out.println("value: " + value))
		.signal(valueSignal)
		.attach(this);
	}

}