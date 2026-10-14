package dev.joid.demo.ui.selector;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.node.impl.structure.checkbox.DemoCheckboxNode;
import dev.joid.demo.ui.node.impl.structure.selector.DemoSelectorNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.selector.SelectorNode.SelectorDirection;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoSelector extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoSelector.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final StringSignal down = new StringSignal("First");
		final StringSignal shared = new StringSignal("Second");
		final IntegerSignal refused = new IntegerSignal(0);
		final BooleanSignal open = new BooleanSignal(false);
		final IntegerSignal opens = new IntegerSignal(0);
		final IntegerSignal refusedOpens = new IntegerSignal(0);

		RectNode
		.create(100, 210, 400, 260)
		.color(UIDemoSelector.PLACEHOLDER)
		.body(rect -> {
			DemoSelectorNode
			.create(40, 30, 320, 50)
			.values("First", "Second", "Third")
			.onChange(event -> {
				System.out.println("[UIDemoSelector] down selector value: " + event.getValue());
				down.set(event.getValue());
			})
			.attach(rect);
			TextNode.create(40, 200).text(Text.create("Value: " + down.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Down", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 210, 400, 260)
		.color(UIDemoSelector.PLACEHOLDER)
		.body(rect -> {
			DemoSelectorNode
			.create(40, 180, 320, 50)
			.direction(SelectorDirection.UP)
			.values("First", "Second", "Third")
			.onChange(event -> System.out.println("[UIDemoSelector] up selector value: " + event.getValue()))
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Up", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 210, 400, 260)
		.color(UIDemoSelector.PLACEHOLDER)
		.body(rect -> {
			DemoSelectorNode.create(40, 30, 320, 50).values("First", "Second", "Third").value("Second").active(true).attach(rect);
			TextNode.create(200, 275).text(Text.create("Open", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 210, 400, 260)
		.color(UIDemoSelector.PLACEHOLDER)
		.body(rect -> {
			DemoSelectorNode.create(40, 30, 220, 50).values("First", "Second", "Third").active(open).onOpen(event -> opens.increment()).attach(rect);
			DemoCheckboxNode.create(300, 30, 50, 50).checked(open).attach(rect);
			TextNode.create(40, 200).text(Text.create("Opened: " + opens.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Bound open", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 550, 400, 260)
		.color(UIDemoSelector.PLACEHOLDER)
		.body(rect -> {
			DemoSelectorNode.create(40, 30, 150, 50).values("First", "Second", "Third").value(shared).attach(rect);
			DemoSelectorNode.create(210, 30, 150, 50).values("First", "Second", "Third").value(shared).attach(rect);
			TextNode.create(40, 200).text(Text.create("Value: " + shared.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Shared signal", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 550, 400, 260)
		.color(UIDemoSelector.PLACEHOLDER)
		.body(rect -> {
			DemoSelectorNode
			.create(40, 30, 320, 50)
			.values("First", "Second", "Third")
			.beforeChange(event -> {
				if ("Third".equals(event.getValue())) {
					refused.increment();
					event.cancel();
				}
			})
			.attach(rect);
			TextNode.create(40, 200).text(Text.create("Refused: " + refused.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Refused", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 550, 400, 260)
		.color(UIDemoSelector.PLACEHOLDER)
		.body(rect -> {
			final DemoSelectorNode locked = DemoSelectorNode
			.create(40, 30, 220, 50)
			.values("First", "Second", "Third")
			.beforeOpen(event -> {
				refusedOpens.increment();
				event.cancel();
			})
			.attach(rect);
			RectNode
			.create(280, 30, 90, 50)
			.color(UIDemoSelector.INK)
			.onClick(event -> locked.active(true))
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Code", label).align(Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 200).text(Text.create("Refused: " + refusedOpens.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Refused open", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 550, 400, 260)
		.color(UIDemoSelector.PLACEHOLDER)
		.body(rect -> {
			DemoSelectorNode.create(40, 30, 320, 50).values("First", "Second", "Third").value("Second").enabled(false).attach(rect);
			TextNode.create(200, 275).text(Text.create("Disabled", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}