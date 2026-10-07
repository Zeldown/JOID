package dev.joid.demo.ui.textfield;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.font.markup.DemoTextMarkup;
import dev.joid.demo.ui.textfield.node.DemoIntegerFieldNode;
import dev.joid.demo.ui.textfield.node.DemoMultilineTextFieldNode;
import dev.joid.demo.ui.textfield.node.DemoTextFieldNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.utils.signal.impl.primitive.StringSignal;

public class UIDemoTextField extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoTextField.INK);
		final TextInfo markup = info.copy().markups(DemoTextMarkup.inst());
		final StringSignal typed = StringSignal.of("");
		final BooleanSignal valid = BooleanSignal.of(true);
		final IntegerSignal number = IntegerSignal.of(7);
		final StringSignal empty = StringSignal.of("null");
		final IntegerSignal focuses = IntegerSignal.of(0);
		final IntegerSignal blurs = IntegerSignal.of(0);
		final IntegerSignal enters = IntegerSignal.of(0);

		RectNode
		.create(80, 40, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode
			.create(40, 40, 240, 50)
			.info(info)
			.marginHorizontal(12D)
			.placeholder("Placeholder")
			.onChange((field, text, value, accepted) -> {
				System.out.println("[UIDemoTextField] text field: " + text);
				typed.set(text);
			})
			.attach(rect);
			TextNode.create(40, 130).text(Text.create("Typed: " + typed.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Text", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 40, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode.create(40, 40, 240, 50).info(markup).marginHorizontal(12D).markup(true).text("<b>bold</b> <c=ff5555>red</c>").attach(rect);
			TextNode.create(160, 275).text(Text.create("Markup", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 40, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode.create(40, 40, 240, 50).info(markup).marginHorizontal(12D).text("<b>bold</b> <c=ff5555>red</c>").attach(rect);
			TextNode.create(160, 275).text(Text.create("Raw tags", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 40, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode.create(40, 40, 240, 50).info(info).marginHorizontal(12D).placeholder("8 at most").maxTextLength(8).attach(rect);
			TextNode.create(160, 275).text(Text.create("Max length", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 40, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode
			.create(40, 40, 240, 50)
			.info(info)
			.marginHorizontal(12D)
			.placeholder("Letters")
			.accept(text -> text.chars().allMatch(Character::isLetter))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Letters only", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 380, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode
			.create(40, 40, 240, 50)
			.format(text -> text.trim().toUpperCase())
			.info(info)
			.marginHorizontal(12D)
			.placeholder("Upper case")
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Format", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 380, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode.create(40, 20, 240, 50).horizontalAlign(Align.START).info(info).marginHorizontal(12D).text("Start").attach(rect);
			DemoTextFieldNode.create(40, 90, 240, 50).horizontalAlign(Align.CENTER).info(info).marginHorizontal(12D).text("Center").attach(rect);
			DemoTextFieldNode.create(40, 160, 240, 50).horizontalAlign(Align.END).info(info).marginHorizontal(12D).text("End").attach(rect);
			TextNode.create(160, 275).text(Text.create("Horizontal align", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 380, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode.create(40, 40, 70, 180).verticalAlign(Align.START).info(info).marginHorizontal(12D).text("Aa").attach(rect);
			DemoTextFieldNode.create(125, 40, 70, 180).verticalAlign(Align.CENTER).info(info).marginHorizontal(12D).text("Aa").attach(rect);
			DemoTextFieldNode.create(210, 40, 70, 180).verticalAlign(Align.END).info(info).marginHorizontal(12D).text("Aa").attach(rect);
			TextNode.create(160, 275).text(Text.create("Vertical align", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 380, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoMultilineTextFieldNode
			.create(40, 40, 240, 180)
			.info(info)
			.margin(12D)
			.placeholder("Placeholder")
			.onChange((field, text, value, accepted) -> System.out.println("[UIDemoTextField] multiline field: " + text))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Multiline", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 380, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoMultilineTextFieldNode
			.create(40, 40, 240, 180)
			.info(markup)
			.margin(12D)
			.markup(true)
			.text("<b>Bold words</b> wrap with <c=ff5555>their color on every line</c>")
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Multiline markup", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 720, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerFieldNode
			.create(40, 40, 240, 50)
			.min(0)
			.max(100)
			.step(5)
			.value(42)
			.info(info)
			.marginHorizontal(12D)
			.onChange((field, text, value, accepted) -> valid.set(accepted))
			.attach(rect);
			TextNode.create(40, 130).text(Text.create(valid.get() ? "In range" : "Out of range", info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("0 to 100, step 5", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 720, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerFieldNode.create(40, 40, 240, 50).info(info).marginHorizontal(12D).signal(number).attach(rect);
			TextNode.create(40, 130).text(Text.create("Doubled: " + number.get() * 2, info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Integer signal", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 720, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerFieldNode
			.create(40, 40, 240, 50)
			.info(info)
			.marginHorizontal(12D)
			.placeholder("Empty")
			.allowEmpty(true)
			.onChange((field, text, value, accepted) -> empty.set(String.valueOf(value)))
			.attach(rect);
			TextNode.create(40, 130).text(Text.create("Value: " + empty.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Empty integer", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 720, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode
			.create(40, 40, 240, 50)
			.onEnter((field, text) -> enters.increment())
			.info(info)
			.marginHorizontal(12D)
			.placeholder("Press Enter")
			.focused(true)
			.onFocus(field -> {
				if (field.isFocused()) {
					focuses.increment();
				} else {
					blurs.increment();
				}
			})
			.attach(rect);
			TextNode.create(40, 115).text(Text.create("Focus: " + focuses.get(), info)).attach(rect);
			TextNode.create(40, 155).text(Text.create("Blur: " + blurs.get(), info)).attach(rect);
			TextNode.create(40, 195).text(Text.create("Enter: " + enters.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Focus and Enter", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 720, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode.create(40, 40, 240, 50).info(info).marginHorizontal(12D).text("Read only").enabled(false).attach(rect);
			TextNode.create(160, 275).text(Text.create("Disabled", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}