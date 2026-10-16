package dev.joid.demo.ui.event;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.node.impl.control.checkbox.DemoCheckboxNode;
import dev.joid.demo.ui.node.impl.control.field.DemoTextFieldNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.node.property.DraggableProperty;
import dev.joid.lib.ui.node.property.OverflowProperty;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoEventFlow extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoEventFlow.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final StringSignal order = new StringSignal("none");
		final IntegerSignal clicks = new IntegerSignal(0);
		final IntegerSignal heard = new IntegerSignal(0);
		final IntegerSignal monitored = new IntegerSignal(0);
		final BooleanSignal guarded = new BooleanSignal(false);
		final IntegerSignal refused = new IntegerSignal(0);
		final StringSignal key = new StringSignal("none");
		final BooleanSignal locked = new BooleanSignal(true);
		final IntegerSignal kept = new IntegerSignal(0);
		final DoubleSignal moveX = new DoubleSignal(40D);
		final DoubleSignal scroll = new DoubleSignal(0D);
		final BooleanSignal focused = new BooleanSignal(false);
		final IntegerSignal cursor = new IntegerSignal(0);
		final IntegerSignal letter = new IntegerSignal(0);
		final BooleanSignal left = new BooleanSignal(false);
		final BooleanSignal right = new BooleanSignal(false);
		final DoubleSignal walkX = new DoubleSignal(40D);

		super.handleKeyPressed(event -> key.set(event.getKey().name()));
		super.handleKeyPressed(event -> {
			if (event.getKey() == Key.Z) {
				letter.increment();
			}

			if (event.getPhysicalKey() == Key.A) {
				left.set(true);
			}

			if (event.getPhysicalKey() == Key.D) {
				right.set(true);
			}
		});
		super.handleKeyReleased(event -> {
			if (event.getPhysicalKey() == Key.A) {
				left.set(false);
			}

			if (event.getPhysicalKey() == Key.D) {
				right.set(false);
			}
		});
		super.beforeClose(EventListener.priority(EventPriority.HIGH, event -> {
			if (locked.peek()) {
				kept.increment();
				event.cancel();
			}
		}));

		RectNode
		.create(80, 40, 320, 260)
		.color(UIDemoEventFlow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoEventFlow.INK)
			.onClick(event -> order.set(order.peek() + ", normal"))
			.onClick(EventListener.priority(EventPriority.HIGH, event -> order.set("high")))
			.label(Text.create("Click", label))
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Order: " + order.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Priority", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 40, 320, 260)
		.color(UIDemoEventFlow.PLACEHOLDER)
		.body(rect -> {
			ContainerNode
			.create(0, 0, 320, 260)
			.handleMousePressed(event -> heard.increment())
			.handleMousePressed(EventListener.priority(EventPriority.MONITOR, event -> monitored.increment()))
			.body(container -> {
				RectNode
				.create(40, 40, 120, 50)
				.color(UIDemoEventFlow.INK)
				.onClick(event -> clicks.increment())
				.label(Text.create("Click", label))
				.attach(container);
			})
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Button: " + clicks.get(), info)).attach(rect);
			TextNode.create(40, 165).text(Text.create("Card: " + heard.get(), info)).attach(rect);
			TextNode.create(40, 210).text(Text.create("Monitor: " + monitored.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Monitor", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 40, 320, 260)
		.color(UIDemoEventFlow.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode
			.create(40, 40, 240, 50)
			.info(info)
			.marginHorizontal(12D)
			.placeholder("No digits")
			.handleCharTyped(event -> {
				if (Character.isDigit(event.getCodepoint())) {
					event.consume();
				}
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Consume", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 40, 320, 260)
		.color(UIDemoEventFlow.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode
			.create(40, 40, 60, 60)
			.checked(guarded)
			.beforeChange(event -> {
				refused.increment();
				event.cancel();
			})
			.attach(rect);
			RectNode
			.create(140, 45, 120, 50)
			.color(UIDemoEventFlow.INK)
			.onClick(event -> guarded.set(!guarded.peek()))
			.label(Text.create("Code", label))
			.attach(rect);
			TextNode.create(40, 140).text(Text.create("Refused: " + refused.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("User only", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 40, 320, 260)
		.color(UIDemoEventFlow.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode.create(40, 40, 240, 50).info(info).marginHorizontal(12D).placeholder("Takes the keys").attach(rect);
			TextNode.create(40, 120).text(Text.create("UI: " + key.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("UI listener", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 380, 320, 260)
		.color(UIDemoEventFlow.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode.create(40, 40, 60, 60).checked(locked).attach(rect);
			TextNode.create(120, 55).text(Text.create("Locked", info)).attach(rect);
			TextNode.create(40, 140).text(Text.create("Kept open: " + kept.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Before close", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 380, 320, 260)
		.color(UIDemoEventFlow.PLACEHOLDER)
		.body(rect -> {
			final RectNode square = RectNode
			.create(40, 40, 60, 60)
			.color(UIDemoEventFlow.INK)
			.x(moveX)
			.draggable(DraggableProperty.parent())
			.beforeMove(event -> {
				if (event.getValue().getX() > 200D) {
					event.cancel();
				}
			})
			.attach(rect);
			RectNode
			.create(170, 190, 110, 40)
			.color(UIDemoEventFlow.INK)
			.onClick(event -> square.move(40D, 40D))
			.label(Text.create("Reset", label))
			.attach(rect);
			TextNode.create(40, 198).text(Text.create("x: " + Math.round(moveX.get()), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Before move", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 380, 320, 260)
		.color(UIDemoEventFlow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 160, 120)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.scrollY(scroll)
			.beforeScroll(event -> {
				if (event.getOffset() < -120D) {
					event.cancel();
				}
			})
			.body(area -> {
				FlexNode
				.vertical(10, 10, 140)
				.margin(10D)
				.body(flex -> {
					for (int i = 0; i < 8; i++) {
						RectNode.create(0, 0, 140, 30).color(i % 2 == 0 ? UIDemoEventFlow.INK : UIDemoEventFlow.PLACEHOLDER).attach(flex);
					}
				})
				.attach(area);
			})
			.attach(rect);
			RectNode
			.create(215, 40, 80, 50)
			.color(UIDemoEventFlow.INK)
			.onClick(event -> scroll.set(-200D))
			.label(Text.create("Code", label))
			.attach(rect);
			TextNode.create(40, 190).text(Text.create("Offset: " + Math.round(scroll.get()), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Before scroll", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 380, 320, 260)
		.color(UIDemoEventFlow.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode
			.create(40, 40, 240, 50)
			.info(info)
			.marginHorizontal(12D)
			.placeholder("Required")
			.focused(focused)
			.beforeBlur(event -> {
				if (event.getNode().getText().isEmpty()) {
					event.cancel();
				}
			})
			.attach(rect);
			DemoCheckboxNode.create(40, 130, 50, 50).checked(focused).attach(rect);
			TextNode.create(110, 142).text(Text.create(focused.get() ? "Focused" : "Blurred", info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Before blur", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 380, 320, 260)
		.color(UIDemoEventFlow.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode
			.create(40, 40, 240, 50)
			.info(info)
			.marginHorizontal(12D)
			.text("Hello world")
			.cursorPosition(cursor)
			.beforeCursorMove(event -> {
				if (event.getValue() > 5) {
					event.cancel();
				}
			})
			.attach(rect);
			RectNode
			.create(170, 130, 110, 40)
			.color(UIDemoEventFlow.INK)
			.onClick(event -> cursor.set(11))
			.label(Text.create("Code", label))
			.attach(rect);
			TextNode.create(40, 138).text(Text.create("Cursor: " + cursor.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Before cursor", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 720, 320, 260)
		.color(UIDemoEventFlow.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(40, 40, 60, 60).color(UIDemoEventFlow.INK).x(walkX).onUpdate(event -> walkX.set(Math.max(40D, Math.min(220D, walkX.peek() + ((right.peek() ? 240D : 0D) - (left.peek() ? 240D : 0D)) * super.getFrameTime() / 1000D)))).attach(rect);
			TextNode.create(40, 130).text(Text.create("Letter Z: " + letter.get(), info)).attach(rect);
			TextNode.create(40, 175).text(Text.create("Move: A / D", info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Letter vs position", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}