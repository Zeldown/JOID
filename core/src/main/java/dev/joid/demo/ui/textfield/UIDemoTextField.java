package dev.joid.demo.ui.textfield;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.textfield.node.DemoIntegerFieldNode;
import dev.joid.demo.ui.textfield.node.DemoMultilineTextFieldNode;
import dev.joid.demo.ui.textfield.node.DemoTextFieldNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;

public class UIDemoTextField extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoTextField.INK);
		final IntegerSignal number = IntegerSignal.of(7);

		RectNode
		.create(140, 110, 520, 300)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode
			.create(40, 125, 440, 50)
			.info(info)
			.marginHorizontal(12D)
			.placeholder("Placeholder")
			.onChange((field, text, value, valid) -> System.out.println("[UIDemoTextField] text field: " + text))
			.attach(rect);
			TextNode.create(260, 315).text(Text.create("Text", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(700, 110, 520, 300)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoMultilineTextFieldNode
			.create(40, 40, 440, 220)
			.info(info)
			.margin(12D)
			.placeholder("Placeholder")
			.onChange((field, text, value, valid) -> System.out.println("[UIDemoTextField] multiline field: " + text))
			.attach(rect);
			TextNode.create(260, 315).text(Text.create("Multiline", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1260, 110, 520, 300)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerFieldNode
			.create(40, 125, 440, 50)
			.min(0)
			.max(100)
			.value(42)
			.info(info)
			.marginHorizontal(12D)
			.attach(rect);
			TextNode.create(260, 315).text(Text.create("Integer 0 to 100", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(420, 520, 520, 300)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerFieldNode
			.create(40, 70, 440, 50)
			.info(info)
			.marginHorizontal(12D)
			.signal(number)
			.attach(rect);
			TextNode.create(40, 170).text(Text.create("Doubled: " + number.get() * 2, info)).attach(rect);
			TextNode.create(260, 315).text(Text.create("Integer signal", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 520, 520, 300)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerFieldNode
			.create(40, 125, 440, 50)
			.info(info)
			.marginHorizontal(12D)
			.placeholder("Empty")
			.allowEmpty(true)
			.attach(rect);
			TextNode.create(260, 315).text(Text.create("Empty integer", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}