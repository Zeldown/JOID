package dev.joid.demo.ui.cursor;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.textfield.node.DemoTextFieldNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.cursor.Cursor;
import dev.joid.lib.utils.signal.impl.primitive.BooleanSignal;

public class UIDemoCursor extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoCursor.INK);
		final BooleanSignal locked = BooleanSignal.of(false);
		final String[] captions = {"Default", "Pointer", "Text", "Crosshair", "Move", "Not allowed", "Resize EW", "Resize NS", "Resize NWSE", "Resize NESW"};
		final Cursor[] cursors = Cursor.values();

		for (int i = 0; i < cursors.length; i++) {
			final int index = i;
			RectNode
			.create(80 + index % 5 * 360, 40 + index / 5 * 340, 320, 260)
			.color(UIDemoCursor.PLACEHOLDER)
			.body(rect -> {
				RectNode.create(100, 50, 120, 120).color(UIDemoCursor.INK).hoveredColor(Color.WHITE).cursor(cursors[index]).attach(rect);
				TextNode.create(160, 275).text(Text.create(captions[index], info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
			})
			.attach(this);
		}

		RectNode
		.create(80, 720, 320, 260)
		.color(UIDemoCursor.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 30, 240, 180)
			.color(Color.WHITE)
			.cursor(Cursor.CROSSHAIR)
			.body(parent -> {
				RectNode.create(70, 40, 100, 100).color(UIDemoCursor.INK).hoveredColor(UIDemoCursor.PLACEHOLDER).attach(parent);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Inherited", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 720, 320, 260)
		.color(UIDemoCursor.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 30, 240, 180)
			.color(Color.WHITE)
			.cursor(Cursor.CROSSHAIR)
			.body(parent -> {
				RectNode.create(70, 40, 100, 100).color(UIDemoCursor.INK).hoveredColor(UIDemoCursor.PLACEHOLDER).cursor(Cursor.POINTER).attach(parent);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Overridden", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 720, 320, 260)
		.color(UIDemoCursor.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(100, 50, 120, 120).color(UIDemoCursor.INK).hoveredColor(Color.WHITE).cursor(Cursor.MOVE).draggable(DraggableProperty.free()).attach(rect);
			TextNode.create(160, 275).text(Text.create("Drag", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 720, 320, 260)
		.color(UIDemoCursor.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 50, 120, 120)
			.color(() -> locked.get() ? UIDemoCursor.INK.copyAlpha(0.4F) : UIDemoCursor.INK)
			.cursor(() -> locked.get() ? Cursor.NOT_ALLOWED : Cursor.POINTER)
			.onClick((node, mouseX, mouseY, clickType) -> locked.set(!locked.get()))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Reactive", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 720, 320, 260)
		.color(UIDemoCursor.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode.create(40, 40, 240, 50).info(info).marginHorizontal(12D).placeholder("Text field").attach(rect);
			TextNode.create(160, 275).text(Text.create("Text field", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}