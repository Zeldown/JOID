package dev.joid.demo.ui.selector;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.selector.node.DemoSelectorNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.selector.SelectorNode.SelectorDirection;
import dev.joid.lib.ui.node.impl.structure.selector.callback.NodeSelectorChangeCallback;
import dev.joid.lib.utils.align.Align;
import lombok.NonNull;

public class UIDemoSelector extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoSelector.INK);
		final StringSignal down = StringSignal.of("First");
		final StringSignal shared = StringSignal.of("Second");
		final IntegerSignal refused = IntegerSignal.of(0);

		RectNode
		.create(320, 210, 400, 260)
		.color(UIDemoSelector.PLACEHOLDER)
		.body(rect -> {
			DemoSelectorNode
			.create(40, 30, 320, 50)
			.values("First", "First", "Second", "Third")
			.onChange((node, value) -> {
				System.out.println("[UIDemoSelector] down selector value: " + value);
				down.set(value);
			})
			.attach(rect);
			TextNode.create(40, 200).text(Text.create("Value: " + down.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Down", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(760, 210, 400, 260)
		.color(UIDemoSelector.PLACEHOLDER)
		.body(rect -> {
			DemoSelectorNode
			.create(40, 180, 320, 50)
			.direction(SelectorDirection.UP)
			.values("First", "First", "Second", "Third")
			.onChange((node, value) -> System.out.println("[UIDemoSelector] up selector value: " + value))
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Up", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1200, 210, 400, 260)
		.color(UIDemoSelector.PLACEHOLDER)
		.body(rect -> {
			DemoSelectorNode.create(40, 30, 320, 50).values("Second", "First", "Second", "Third").active(true).attach(rect);
			TextNode.create(200, 275).text(Text.create("Open", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(320, 550, 400, 260)
		.color(UIDemoSelector.PLACEHOLDER)
		.body(rect -> {
			DemoSelectorNode.create(40, 30, 150, 50).values("Second", "First", "Second", "Third").signal(shared).attach(rect);
			DemoSelectorNode.create(210, 30, 150, 50).values("Second", "First", "Second", "Third").signal(shared).attach(rect);
			TextNode.create(40, 200).text(Text.create("Value: " + shared.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Shared signal", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(760, 550, 400, 260)
		.color(UIDemoSelector.PLACEHOLDER)
		.body(rect -> {
			DemoSelectorNode
			.create(40, 30, 320, 50)
			.values("First", "First", "Second", "Third")
			.onChange(new NodeSelectorChangeCallback<DemoSelectorNode, String>() {

				@Override
				public void apply(final @NonNull DemoSelectorNode node, final @NonNull String value) {}

				@Override
				public void pre(final @NonNull DemoSelectorNode node, final @NonNull DispatchContext context, final @NonNull String value) {
					if ("Third".equals(value)) {
						refused.increment();
						context.cancel();
					}
				}

			})
			.attach(rect);
			TextNode.create(40, 200).text(Text.create("Refused: " + refused.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Refused", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1200, 550, 400, 260)
		.color(UIDemoSelector.PLACEHOLDER)
		.body(rect -> {
			DemoSelectorNode.create(40, 30, 320, 50).values("Second", "First", "Second", "Third").enabled(false).attach(rect);
			TextNode.create(200, 275).text(Text.create("Disabled", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}