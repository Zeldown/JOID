package dev.joid.demo.ui.textfield;

import java.util.Locale;

import dev.joid.demo.DemoFont;
import dev.joid.demo.font.markup.DemoTextMarkup;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.node.impl.control.field.DemoIntegerFieldNode;
import dev.joid.demo.ui.node.impl.control.field.DemoMultilineTextFieldNode;
import dev.joid.demo.ui.node.impl.control.field.DemoTextFieldNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoTextField extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color ACCENT      = new Color(255, 85, 85).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoTextField.INK);
		final TextInfo markup = info.copy().markups(DemoTextMarkup.inst());
		final StringSignal typed = new StringSignal("");
		final StringSignal key = new StringSignal("-");
		final StringSignal character = new StringSignal("-");
		final BooleanSignal valid = new BooleanSignal(true);
		final IntegerSignal number = new IntegerSignal(7);
		final StringSignal empty = new StringSignal("null");
		final IntegerSignal focuses = new IntegerSignal(0);
		final IntegerSignal blurs = new IntegerSignal(0);
		final IntegerSignal enters = new IntegerSignal(0);

		RectNode
		.create(80, 40, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode
			.create(40, 40, 240, 50)
			.info(info)
			.marginHorizontal(12D)
			.placeholder("Placeholder")
			.onChange(event -> typed.set(event.getText()))
			.<DemoTextFieldNode>handleKeyPressed(event -> {
				if (event.getNode().isFocused()) {
					key.set(event.getKey().name());
				}
			})
			.<DemoTextFieldNode>handleCharTyped(event -> {
				if (event.getNode().isFocused()) {
					character.set(String.format("U+%04X", event.getCodepoint()));
				}
			})
			.attach(rect);
			TextNode.create(40, 130).text(Text.create("Typed: " + typed.get(), info)).attach(rect);
			TextNode.create(40, 170).text(Text.create("Key: " + key.get() + "  Char: " + character.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Text", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 40, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode.create(40, 40, 240, 50).info(markup).marginHorizontal(12D).markup(true).text("<b>bold</b> <c=ff5555>red</c>").attach(rect);
			DemoTextFieldNode.create(40, 120, 240, 50).info(markup).marginHorizontal(12D).text("<b>bold</b> <c=ff5555>red</c>").attach(rect);
			TextNode.create(160, 275).text(Text.create("Markup and raw", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 40, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode
			.create(40, 40, 240, 50)
			.info(info)
			.marginHorizontal(12D)
			.placeholder("Placeholder")
			.placeholderColor(UIDemoTextField.ACCENT)
			.cursorColor(UIDemoTextField.ACCENT)
			.selectionColor(UIDemoTextField.ACCENT.withAlpha(0.3F))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Colors", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 40, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode.create(40, 40, 240, 50).info(info).marginHorizontal(12D).placeholder("8 at most").maxTextLength(8).attach(rect);
			TextNode.create(160, 275).text(Text.create("Max length", info)).anchorX(Align.CENTER).attach(rect);
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
			TextNode.create(160, 275).text(Text.create("Letters only", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 380, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode
			.create(40, 40, 240, 50)
			.normalize(text -> text.trim().toUpperCase(Locale.ROOT))
			.info(info)
			.marginHorizontal(12D)
			.placeholder("Upper case")
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Normalize", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 380, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode.create(40, 20, 240, 50).alignX(Align.START).info(info).marginHorizontal(12D).text("Start").attach(rect);
			DemoTextFieldNode.create(40, 90, 240, 50).alignX(Align.CENTER).info(info).marginHorizontal(12D).text("Center").attach(rect);
			DemoTextFieldNode.create(40, 160, 240, 50).alignX(Align.END).info(info).marginHorizontal(12D).text("End").attach(rect);
			TextNode.create(160, 275).text(Text.create("Horizontal align", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 380, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode.create(40, 40, 70, 180).alignY(Align.START).info(info).marginHorizontal(12D).text("Aa").attach(rect);
			DemoTextFieldNode.create(125, 40, 70, 180).alignY(Align.CENTER).info(info).marginHorizontal(12D).text("Aa").attach(rect);
			DemoTextFieldNode.create(210, 40, 70, 180).alignY(Align.END).info(info).marginHorizontal(12D).text("Aa").attach(rect);
			TextNode.create(160, 275).text(Text.create("Vertical align", info)).anchorX(Align.CENTER).attach(rect);
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
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Multiline", info)).anchorX(Align.CENTER).attach(rect);
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
			TextNode.create(160, 275).text(Text.create("Multiline markup", info)).anchorX(Align.CENTER).attach(rect);
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
			.onChange(event -> valid.set(event.isValid()))
			.attach(rect);
			TextNode.create(40, 130).text(Text.create(valid.get() ? "In range" : "Out of range", info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("0 to 100, step 5", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 720, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerFieldNode.create(40, 40, 240, 50).value(number).info(info).marginHorizontal(12D).attach(rect);
			TextNode.create(40, 130).text(Text.create("Doubled: " + number.get() * 2, info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Integer signal", info)).anchorX(Align.CENTER).attach(rect);
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
			.onChange(event -> empty.set(String.valueOf(event.getValue())))
			.attach(rect);
			TextNode.create(40, 130).text(Text.create("Value: " + empty.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Empty integer", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 720, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode
			.create(40, 40, 240, 50)
			.onSubmit(event -> enters.increment())
			.info(info)
			.marginHorizontal(12D)
			.placeholder("Press Enter")
			.focused(true)
			.onFocus(event -> focuses.increment())
			.onBlur(event -> blurs.increment())
			.attach(rect);
			TextNode.create(40, 115).text(Text.create("Focus: " + focuses.get(), info)).attach(rect);
			TextNode.create(40, 155).text(Text.create("Blur: " + blurs.get(), info)).attach(rect);
			TextNode.create(40, 195).text(Text.create("Enter: " + enters.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Focus and Enter", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 720, 320, 260)
		.color(UIDemoTextField.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode.create(40, 40, 240, 50).info(info).marginHorizontal(12D).text("Read only").enabled(false).attach(rect);
			TextNode.create(160, 275).text(Text.create("Disabled", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}