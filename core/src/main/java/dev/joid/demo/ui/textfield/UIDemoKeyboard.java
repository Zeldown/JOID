package dev.joid.demo.ui.textfield;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.textfield.node.DemoTextFieldNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.impl.key.NodeCharTypedCallback;
import dev.joid.lib.ui.node.callback.impl.key.NodeKeyPressedCallback;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class UIDemoKeyboard extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoKeyboard.INK);
		final BooleanSignal focused = BooleanSignal.of(false);
		final StringSignal key = StringSignal.of("-");
		final StringSignal character = StringSignal.of("-");
		final StringSignal codepoint = StringSignal.of("-");

		RectNode
		.create(80, 40, 320, 260)
		.color(UIDemoKeyboard.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 240, 180)
			.color(Color.WHITE)
			.cursor(Cursor.POINTER)
			.onMousePressed((box, mouseX, mouseY, button) -> focused.set(box.isHovered(mouseX, mouseY)))
			.onKeyPressed(new NodeKeyPressedCallback<RectNode>() {

				@Override
				public void apply(final RectNode box, final Key pressed) {
					key.set(pressed.name());
				}

				@Override
				public void post(final RectNode box, final DispatchContext context, final Key pressed) {
					if (!context.isCancelled() && focused.peek()) {
						context.cancel(() -> this.apply(box, pressed));
					}
				}

			})
			.onCharTyped(new NodeCharTypedCallback<RectNode>() {

				@Override
				public void apply(final RectNode box, final int typed) {
					character.set(new String(Character.toChars(typed)));
				}

				@Override
				public void post(final RectNode box, final DispatchContext context, final int typed) {
					if (!context.isCancelled() && focused.peek()) {
						context.cancel(() -> this.apply(box, typed));
					}
				}

			})
			.body(box -> {
				TextNode.create(20, 35).text(Text.create(focused.get() ? "Focused" : "Click me", info)).attach(box);
				TextNode.create(20, 90).text(Text.create("Key: " + key.get(), info)).attach(box);
				TextNode.create(20, 145).text(Text.create("Char: " + character.get(), info)).attach(box);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Key then char", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 40, 320, 260)
		.color(UIDemoKeyboard.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode
			.create(40, 40, 240, 50)
			.info(info)
			.marginHorizontal(12D)
			.placeholder("Type an emoji")
			.onCharTyped(new NodeCharTypedCallback<DemoTextFieldNode>() {

				@Override
				public void apply(final DemoTextFieldNode field, final int typed) {}

				@Override
				public void pre(final DemoTextFieldNode field, final DispatchContext context, final int typed) {
					if (!context.isCancelled() && field.isFocused()) {
						codepoint.set(String.format("U+%04X", typed));
					}
				}

			})
			.attach(rect);
			TextNode.create(40, 130).text(Text.create("Last: " + codepoint.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("One code point", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}