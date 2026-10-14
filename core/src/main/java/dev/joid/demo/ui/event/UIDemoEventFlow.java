package dev.joid.demo.ui.event;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.node.impl.design.textfield.DemoTextFieldNode;
import dev.joid.demo.ui.node.impl.structure.checkbox.DemoCheckboxNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoEventFlow extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).freeze();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).freeze();

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

		super.handleKeyPressed(event -> key.set(event.getKey().name()));
		super.beforeClose(EventPriority.HIGH, event -> {
			if (locked.peek()) {
				kept.increment();
				event.cancel();
			}
		});

		RectNode
		.create(80, 40, 320, 260)
		.color(UIDemoEventFlow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoEventFlow.INK)
			.onClick(event -> order.set(order.peek() + ", normal"))
			.onClick(EventPriority.HIGH, event -> order.set("high"))
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Click", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Order: " + order.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Priority", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 40, 320, 260)
		.color(UIDemoEventFlow.PLACEHOLDER)
		.body(rect -> {
			ContainerNode
			.create(0, 0, 320, 260)
			.handleMousePressed(event -> heard.increment())
			.handleMousePressed(EventPriority.MONITOR, event -> monitored.increment())
			.body(container -> {
				RectNode
				.create(40, 40, 120, 50)
				.color(UIDemoEventFlow.INK)
				.onClick(event -> clicks.increment())
				.body(button -> {
					TextNode.create(button.dw(2), button.dh(2)).text(Text.create("Click", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(button);
				})
				.attach(container);
			})
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Button: " + clicks.get(), info)).attach(rect);
			TextNode.create(40, 165).text(Text.create("Card: " + heard.get(), info)).attach(rect);
			TextNode.create(40, 210).text(Text.create("Monitor: " + monitored.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Monitor", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
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
			TextNode.create(160, 275).text(Text.create("Consume", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
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
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Code", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 140).text(Text.create("Refused: " + refused.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("User only", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 40, 320, 260)
		.color(UIDemoEventFlow.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode.create(40, 40, 240, 50).info(info).marginHorizontal(12D).placeholder("Takes the keys").attach(rect);
			TextNode.create(40, 120).text(Text.create("UI: " + key.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("UI listener", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 380, 320, 260)
		.color(UIDemoEventFlow.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode.create(40, 40, 60, 60).checked(locked).attach(rect);
			TextNode.create(120, 55).text(Text.create("Locked", info)).attach(rect);
			TextNode.create(40, 140).text(Text.create("Kept open: " + kept.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Before close", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
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
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Reset", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 198).text(() -> Text.create("x: " + Math.round(moveX.get()), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Before move", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
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
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Code", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 190).text(() -> Text.create("Offset: " + Math.round(scroll.get()), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Before scroll", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
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
			TextNode.create(160, 275).text(Text.create("Before blur", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
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
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Code", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 138).text(Text.create("Cursor: " + cursor.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Before cursor", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}