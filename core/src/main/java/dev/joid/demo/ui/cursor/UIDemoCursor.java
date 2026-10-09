package dev.joid.demo.ui.cursor;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.textfield.node.DemoTextFieldNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty;
import dev.joid.lib.utils.align.Align;

public class UIDemoCursor extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoCursor.INK);
		final TextInfo small = TextInfo.create(DemoFont.MONTSERRAT, 18, UIDemoCursor.INK);
		final BooleanSignal locked = BooleanSignal.of(false);
		final IntegerSignal through = IntegerSignal.of(0);
		final IntegerSignal blocked = IntegerSignal.of(0);
		final IntegerSignal bubbled = IntegerSignal.of(0);
		final String[] captions = {"Default", "Pointer", "Text", "Crosshair", "Move", "Not allowed", "Resize EW", "Resize NS", "Resize NWSE", "Resize NESW"};
		final Cursor[] cursors = Cursor.values();

		for (int i = 0; i < cursors.length; i++) {
			final int index = i;
			RectNode
			.create(80 + index % 5 * 360, 20 + index / 5 * 260, 320, 200)
			.color(UIDemoCursor.PLACEHOLDER)
			.body(rect -> {
				RectNode.create(100, 40, 120, 120).color(UIDemoCursor.INK).hoveredColor(Color.WHITE).cursor(cursors[index]).attach(rect);
				TextNode.create(160, 215).text(Text.create(captions[index], info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
			})
			.attach(this);
		}

		RectNode
		.create(80, 540, 320, 200)
		.color(UIDemoCursor.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 20, 240, 160)
			.color(Color.WHITE)
			.cursor(Cursor.CROSSHAIR)
			.body(parent -> {
				RectNode.create(70, 30, 100, 100).color(UIDemoCursor.INK).hoveredColor(UIDemoCursor.PLACEHOLDER).attach(parent);
			})
			.attach(rect);
			TextNode.create(160, 215).text(Text.create("Inherited", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 540, 320, 200)
		.color(UIDemoCursor.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 20, 240, 160)
			.color(Color.WHITE)
			.cursor(Cursor.CROSSHAIR)
			.body(parent -> {
				RectNode.create(70, 30, 100, 100).color(UIDemoCursor.INK).hoveredColor(UIDemoCursor.PLACEHOLDER).cursor(Cursor.POINTER).attach(parent);
			})
			.attach(rect);
			TextNode.create(160, 215).text(Text.create("Overridden", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 540, 320, 200)
		.color(UIDemoCursor.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(100, 40, 120, 120).color(UIDemoCursor.INK).hoveredColor(Color.WHITE).cursor(Cursor.MOVE).draggable(DraggableProperty.parent()).attach(rect);
			TextNode.create(160, 215).text(Text.create("Drag", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 540, 320, 200)
		.color(UIDemoCursor.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 40, 120, 120)
			.color(() -> locked.get() ? UIDemoCursor.INK.copyAlpha(0.4F) : UIDemoCursor.INK)
			.cursor(() -> locked.get() ? Cursor.NOT_ALLOWED : Cursor.POINTER)
			.onClick((node, mouseX, mouseY, button) -> locked.set(!locked.get()))
			.attach(rect);
			TextNode.create(160, 215).text(Text.create("Reactive", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 540, 320, 200)
		.color(UIDemoCursor.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode.create(40, 75, 240, 50).info(info).marginHorizontal(12D).placeholder("Text field").attach(rect);
			TextNode.create(160, 215).text(Text.create("Text field", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 800, 320, 200)
		.color(UIDemoCursor.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 30, 120, 120)
			.color(UIDemoCursor.INK)
			.hoveredColor(Color.WHITE)
			.cursor(Cursor.POINTER)
			.hover(() -> "Button")
			.onClick((node, mouseX, mouseY, button) -> through.increment())
			.attach(rect);
			RectNode.create(130, 60, 60, 60).color(UIDemoCursor.PLACEHOLDER).interactive(false).attach(rect);
			TextNode.create(160, 160).text(Text.create("Clicks: " + through.get(), small, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
			TextNode.create(160, 215).text(Text.create("Click-through", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 800, 320, 200)
		.color(UIDemoCursor.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 30, 120, 120)
			.color(UIDemoCursor.INK)
			.hoveredColor(Color.WHITE)
			.cursor(Cursor.POINTER)
			.hover(() -> "Button")
			.onClick((node, mouseX, mouseY, button) -> blocked.increment())
			.attach(rect);
			RectNode.create(130, 60, 60, 60).color(UIDemoCursor.PLACEHOLDER).attach(rect);
			TextNode.create(160, 160).text(Text.create("Clicks: " + blocked.get(), small, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
			TextNode.create(160, 215).text(Text.create("Blocked", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 800, 320, 200)
		.color(UIDemoCursor.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(60, 30, 200, 120)
			.color(UIDemoCursor.INK)
			.hoveredColor(Color.WHITE)
			.cursor(Cursor.POINTER)
			.onClick((node, mouseX, mouseY, button) -> bubbled.increment())
			.body(parent -> {
				RectNode.create(60, 30, 80, 60).color(UIDemoCursor.PLACEHOLDER).attach(parent);
			})
			.attach(rect);
			TextNode.create(160, 160).text(Text.create("Clicks: " + bubbled.get(), small, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
			TextNode.create(160, 215).text(Text.create("Bubbling", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 800, 320, 200)
		.color(UIDemoCursor.PLACEHOLDER)
		.body(rect -> {
			for (int i = 0; i < 5; i++) {
				final int line = i;
				TextNode.create(20, 16 + line * 28).text(Text.create(() -> super.getNodeListAt(super.getMouseX(), super.getMouseY()).stream().skip(line).findFirst().map(node -> node.getClass().getSimpleName() + " " + (int) node.getWidth() + "x" + (int) node.getHeight() + (node.isInteractive() ? "" : " through")).orElse(""), small)).attach(rect);
			}
			TextNode.create(160, 215).text(Text.create("Nodes at point", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 800, 320, 200)
		.color(UIDemoCursor.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(160, 70).text(Text.create(() -> "x " + (int) super.getViewX() + "  y " + (int) super.getViewY(), info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
			TextNode.create(160, 110).text(Text.create(() -> (int) super.getViewWidth() + " x " + (int) super.getViewHeight(), info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
			TextNode.create(160, 215).text(Text.create("View edges", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(0, 0, 0, 8)
		.color(UIDemoCursor.INK)
		.x(super::getViewX)
		.y(() -> super.getViewY() + super.getViewHeight() - 8)
		.width(super::getViewWidth)
		.interactive(false)
		.attach(this);
	}

}