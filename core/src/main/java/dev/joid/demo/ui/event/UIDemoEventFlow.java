package dev.joid.demo.ui.event;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.node.impl.design.textfield.DemoTextFieldNode;
import dev.joid.demo.ui.node.impl.structure.checkbox.DemoCheckboxNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoEventFlow extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoEventFlow.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final StringSignal order = StringSignal.of("none");
		final IntegerSignal clicks = IntegerSignal.of(0);
		final IntegerSignal heard = IntegerSignal.of(0);
		final IntegerSignal monitored = IntegerSignal.of(0);
		final BooleanSignal guarded = BooleanSignal.of(false);
		final IntegerSignal refused = IntegerSignal.of(0);
		final StringSignal key = StringSignal.of("none");
		final BooleanSignal locked = BooleanSignal.of(true);
		final IntegerSignal kept = IntegerSignal.of(0);

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
			.signal(guarded)
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
			DemoCheckboxNode.create(40, 40, 60, 60).signal(locked).attach(rect);
			TextNode.create(120, 55).text(Text.create("Locked", info)).attach(rect);
			TextNode.create(40, 140).text(Text.create("Kept open: " + kept.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Before close", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}