package dev.joid.demo.ui.signal;

import java.util.Arrays;
import java.util.Collections;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.checkbox.node.DemoCheckboxNode;
import dev.joid.demo.ui.slider.node.DemoIntegerSliderNode;
import dev.joid.demo.ui.textfield.node.DemoTextFieldNode;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.node.impl.design.progress.ProgressNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.ui.node.property.watch.WatchProperty;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.impl.iterable.ListSignal;
import dev.joid.lib.utils.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;

public class UIDemoSignal extends UIDemo {

	private static final Color PLACEHOLDER = new Color(221, 221, 221);
	private static final Color INK         = new Color(153, 153, 153);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoSignal.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final long opened = BridgeHandler.CLOCK.get().currentTimeMillis();
		final IntegerSignal clicks = IntegerSignal.of(0);
		final IntegerSignal mapped = IntegerSignal.of(0);
		final IntegerSignal left = IntegerSignal.of(0);
		final IntegerSignal right = IntegerSignal.of(0);
		final IntegerSignal volume = IntegerSignal.of(5);
		final BooleanSignal muted = BooleanSignal.of(false);
		final Signal<String> name = Signal.of("Ada");
		final BooleanSignal shown = BooleanSignal.of(false);
		final IntegerSignal heat = IntegerSignal.of(0);
		final ListSignal<String> items = new ListSignal<>(Arrays.asList("First item", "Second item"));
		final ListSignal<String> cards = new ListSignal<>(Collections.emptyList());
		final IntegerSignal sent = IntegerSignal.of(0);
		final IntegerSignal saved = IntegerSignal.of(0);

		sent.subscribe(value -> {
			saved.set(value);
			return true;
		});

		RectNode
		.create(100, 40, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick((node, mouseX, mouseY, clickType) -> clicks.increment())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("+1", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Clicks: " + clicks.get(), info)).attach(rect);
			RectNode.create(40, 180, 20, 20).color(UIDemoSignal.INK).width(Math.min(320D, 20D + clicks.get() * 40D)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Native expression", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 40, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick((node, mouseX, mouseY, clickType) -> mapped.increment())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("+1", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 120).text(Text.create(mapped.map(value -> "Doubled: " + value * 2), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("map", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 40, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick((node, mouseX, mouseY, clickType) -> left.increment())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("A +1", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode
			.create(180, 40, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick((node, mouseX, mouseY, clickType) -> right.increment())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("B +1", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 120).text(Text.create(Signal.from(() -> "A + B = " + (left.get() + right.get())), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Signal.from", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 40, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(40, 50).text(Text.create(() -> "Open for " + (BridgeHandler.CLOCK.get().currentTimeMillis() - opened) / 1000L + " s", info)).attach(rect);
			RectNode
			.create(40, 120, 0, 20)
			.color(UIDemoSignal.INK)
			.width(() -> (BridgeHandler.CLOCK.get().currentTimeMillis() - opened) % 2000L / 2000D * 320D)
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Every frame", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 380, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerSliderNode.create(40, 40, 320, 40).values(0, 10, 5).signal(volume).attach(rect);
			ProgressNode
			.create(40, 110, 320, 20)
			.background(Color.WHITE)
			.foreground(UIDemoSignal.INK)
			.progress(volume.get() / 10F)
			.attach(rect);
			RectNode
			.create(40, 160, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick((node, mouseX, mouseY, clickType) -> volume.set(0))
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Reset", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Two-way slider", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 380, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode.create(40, 40, 50, 50).signal(muted).attach(rect);
			TextNode.create(110, 52).text(Text.create(muted.get() ? "Muted" : "Sound on", info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Checkbox", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 380, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode.create(40, 40, 320, 50).info(info).marginHorizontal(12D).signal(name).attach(rect);
			TextNode.create(40, 120).text(Text.create("Hello " + name.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Text field", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 380, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick((node, mouseX, mouseY, clickType) -> shown.toggle())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Toggle", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode.create(40, 120, 80, 80).color(UIDemoSignal.INK).visible(shown).attach(rect);
			TextNode.create(200, 275).text(Text.create("Visibility", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 720, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick((node, mouseX, mouseY, clickType) -> heat.increment())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("+1", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode.create(40, 120, 80, 80).color(heat.get() >= 3 ? UIDemoSignal.INK : Color.WHITE).attach(rect);
			TextNode.create(140, 145).text(Text.create(heat.get() >= 3 ? "3 clicks or more" : "Under 3 clicks", info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Color", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 720, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick((node, mouseX, mouseY, clickType) -> {
				if (items.size() < 4) {
					items.add("Item " + (items.size() + 1));
				}
			})
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Add", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode
			.create(180, 40, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick((node, mouseX, mouseY, clickType) -> items.remove(items.size() - 1))
			.visible(!items.isEmpty())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Remove", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			FlexNode
			.vertical(40, 110, 320)
			.margin(6D)
			.watch(items, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY)
			.body(flex -> {
				for (final String item : items.get()) {
					RectNode
					.create(0, 0, 320, 30)
					.color(Color.WHITE)
					.body(container -> {
						TextNode.create(10, container.dh(2)).text(Text.create(item, info, Align.START, Align.CENTER)).anchorY(Align.CENTER).attach(container);
					})
					.attach(flex);
				}
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("watch", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 720, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 320, 180)
			.color(Color.WHITE)
			.wait(cards)
			.skeleton(container -> RectNode.create(0, 0, container.getWidth(), container.getHeight()).color(Color.LOADING))
			.body(container -> {
				TextNode.create(20, 20).text(Text.create(cards.isEmpty() ? "" : cards.get(0), info)).attach(container);
				TextNode.create(20, 70).text(Text.create(cards.isEmpty() ? "" : cards.get(1), info)).attach(container);
				TextNode.create(20, 120).text(Text.create(cards.isEmpty() ? "" : cards.get(2), info)).attach(container);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("wait", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 720, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick((node, mouseX, mouseY, clickType) -> sent.increment())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Send", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Saved: " + saved.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("subscribe", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		super.schedule(() -> cards.set(Arrays.asList("Ada Lovelace", "Analytical Engine", "1843")), 3000L);
	}

}