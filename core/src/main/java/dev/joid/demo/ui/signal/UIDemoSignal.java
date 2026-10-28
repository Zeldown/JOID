package dev.joid.demo.ui.signal;

import java.util.Arrays;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.node.impl.control.checkbox.DemoCheckboxNode;
import dev.joid.demo.ui.node.impl.control.field.DemoTextFieldNode;
import dev.joid.demo.ui.node.impl.control.slider.DemoIntegerSliderNode;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.impl.iterable.ListSignal;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.progress.ProgressNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.node.property.WatchProperty;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoSignal extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	private final TextInfo plain    = TextInfo.create(DemoFont.MONTSERRAT, 24).color(UIDemoSignal.INK);
	private final TextInfo emphasis = TextInfo.create(DemoFont.MONTSERRAT, 24).color(UIDemoSignal.INK).italic(true);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24).color(UIDemoSignal.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22).color(Color.WHITE);
		final long opened = BridgeHandler.CLOCK.get().currentTimeMillis();
		final IntegerSignal clicks = new IntegerSignal(0);
		final IntegerSignal mapped = new IntegerSignal(0);
		final IntegerSignal left = new IntegerSignal(0);
		final IntegerSignal right = new IntegerSignal(0);
		final IntegerSignal volume = new IntegerSignal(5);
		final BooleanSignal muted = new BooleanSignal(false);
		final Signal<String> name = new Signal<>("Ada");
		final BooleanSignal shown = new BooleanSignal(false);
		final IntegerSignal heat = new IntegerSignal(0);
		final ListSignal<String> items = new ListSignal<>(Arrays.asList("First item", "Second item"));

		RectNode
		.create(100, 40, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick(event -> clicks.increment())
			.label(Text.create("+1", label))
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Clicks: " + clicks.get(), info)).attach(rect);
			RectNode.create(40, 180, 20, 20).color(UIDemoSignal.INK).width(Math.min(320D, 20D + clicks.get() * 40D)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Native expression", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 40, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick(event -> mapped.increment())
			.label(Text.create("+1", label))
			.attach(rect);
			TextNode.create(40, 120).text(Text.create(mapped.map(value -> "Doubled: " + value * 2), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("map", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 40, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick(event -> left.increment())
			.label(Text.create("A +1", label))
			.attach(rect);
			RectNode
			.create(180, 40, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick(event -> right.increment())
			.label(Text.create("B +1", label))
			.attach(rect);
			TextNode.create(40, 120).text(Text.create(Signal.from(() -> "A + B = " + (left.get() + right.get())), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Signal.from", info)).anchorX(Align.CENTER).attach(rect);
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
			TextNode.create(200, 275).text(Text.create("Every frame", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 380, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerSliderNode.create(40, 40, 320, 40).range(0, 10).value(volume).attach(rect);
			ProgressNode
			.create(40, 110, 320, 20)
			.track(RectNode.create(0, 0, 0, 0).color(Color.WHITE))
			.fill(RectNode.create(0, 0, 0, 0).color(UIDemoSignal.INK))
			.progress(volume.get() / 10F)
			.attach(rect);
			RectNode
			.create(40, 160, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick(event -> volume.set(0))
			.label(Text.create("Reset", label))
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Two-way slider", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 380, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode.create(40, 40, 50, 50).checked(muted).attach(rect);
			TextNode.create(110, 52).text(Text.create(muted.get() ? "Muted" : "Sound on", info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Checkbox", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 380, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			DemoTextFieldNode.create(40, 40, 320, 50).info(info).marginHorizontal(12D).text(name).attach(rect);
			TextNode.create(40, 120).text(Text.create("Hello " + name.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Text field", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 380, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick(event -> shown.toggle())
			.label(Text.create("Toggle", label))
			.attach(rect);
			RectNode.create(40, 120, 80, 80).color(UIDemoSignal.INK).visible(shown).attach(rect);
			TextNode.create(200, 275).text(Text.create("Visibility", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 720, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick(event -> heat.increment())
			.label(Text.create("+1", label))
			.attach(rect);
			RectNode.create(40, 120, 80, 80).color(heat.get() >= 3 ? UIDemoSignal.INK : Color.WHITE).attach(rect);
			TextNode.create(140, 145).text(Text.create(heat.get() >= 3 ? "3 clicks or more" : "Under 3 clicks", heat.get() >= 3 ? this.emphasis : this.plain)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Color and style", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 720, 400, 260)
		.color(UIDemoSignal.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick(event -> {
				if (items.size() < 4) {
					items.add("Item " + (items.size() + 1));
				}
			})
			.label(Text.create("Add", label))
			.attach(rect);
			RectNode
			.create(180, 40, 120, 50)
			.color(UIDemoSignal.INK)
			.onClick(event -> items.remove(items.size() - 1))
			.visible(!items.isEmpty())
			.label(Text.create("Remove", label))
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
						TextNode.create(10, container.dh(2)).text(Text.create(item, info).alignX(Align.START).alignY(Align.CENTER)).anchorY(Align.CENTER).attach(container);
					})
					.attach(flex);
				}
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("watch", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}