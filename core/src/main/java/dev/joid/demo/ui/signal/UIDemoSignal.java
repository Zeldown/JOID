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
import dev.joid.lib.utils.signal.ComputedSignal;
import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.impl.iterable.ListSignal;
import dev.joid.lib.utils.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;

public class UIDemoSignal extends UIDemo {

	private static final Color PLACEHOLDER = new Color(221, 221, 221);
	private static final Color INK         = new Color(153, 153, 153);

	private final IntegerSignal      saved  = IntegerSignal.of(0);
	private final IntegerSignal      clicks = IntegerSignal.of(0);
	private final IntegerSignal      volume = IntegerSignal.of(5);
	private final BooleanSignal      muted  = new BooleanSignal(false);
	private final Signal<String>     name   = Signal.of("Ada");
	private final ListSignal<String> items  = new ListSignal<>(Arrays.asList("First item", "Second item"));
	private final ListSignal<String> cards  = new ListSignal<>(Collections.emptyList());

	private final ComputedSignal<Integer> doubled = this.clicks.map(clicks -> clicks * 2);
	private final ComputedSignal<String>  summary = Signal.from(() -> this.name.get() + " doubled to " + this.doubled.get());

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoSignal.INK);
		final TextInfo button = TextInfo.create(DemoFont.MONTSERRAT, 24, Color.BLACK);
		final long opened = BridgeHandler.CLOCK.get().currentTimeMillis();
		final ComputedSignal<String> bonus = Signal.from("Clicks + 10: " + (this.clicks.get() + 10));

		this.doubled.subscribe(doubled -> {
			this.saved.set(doubled);
			return true;
		});

		RectNode
		.create(160, 160, 400, 60)
		.color(UIDemoSignal.PLACEHOLDER)
		.onClick((node, mouseX, mouseY, clickType) -> this.clicks.increment())
		.body(rect -> TextNode.create(rect.dw(2), rect.dh(2)).text(Text.create("+1", button, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(rect))
		.attach(this);
		TextNode.create(160, 240).text(Text.create("Clicks: " + this.clicks.get(), info)).attach(this);
		RectNode.create(160, 290, 40, 20).color(UIDemoSignal.INK).width(40D + this.clicks.get() * 40D).attach(this);
		ProgressNode
		.create(160, 330, 400, 20)
		.background(UIDemoSignal.PLACEHOLDER)
		.foreground(UIDemoSignal.INK)
		.progress(Math.min(1F, this.clicks.get() / 5F))
		.attach(this);
		RectNode.create(160, 370, 60, 60).color(this.clicks.get() >= 3 ? UIDemoSignal.INK : UIDemoSignal.PLACEHOLDER).attach(this);
		RectNode.create(240, 370, 60, 60).color(UIDemoSignal.PLACEHOLDER).visible(this.clicks.get() % 2 == 1).attach(this);

		TextNode.create(760, 160).text(Text.create(this.doubled.map(doubled -> "Doubled: " + doubled), info)).attach(this);
		TextNode.create(760, 210).text(Text.create(bonus, info)).attach(this);
		TextNode.create(760, 260).text(Text.create(this.summary, info)).attach(this);
		TextNode.create(760, 310).text(Text.create(() -> "Open for " + (BridgeHandler.CLOCK.get().currentTimeMillis() - opened) / 1000L + " s", info)).attach(this);
		TextNode.create(760, 360).text(Text.create("Saved: " + this.saved.get(), info)).attach(this);

		DemoCheckboxNode.create(1360, 160, 40, 40).signal(this.muted).attach(this);
		TextNode.create(1420, 168).text(Text.create(this.muted.get() ? "Muted" : "Sound on", info)).attach(this);
		DemoIntegerSliderNode.create(1360, 230, 400, 40).values(1, 9, 5).signal(this.volume).attach(this);
		ProgressNode
		.create(1360, 290, 400, 20)
		.background(UIDemoSignal.PLACEHOLDER)
		.foreground(UIDemoSignal.INK)
		.progress(this.volume.get() / 9F)
		.attach(this);
		DemoTextFieldNode.create(1360, 340, 400, 50).info(info).signal(this.name).attach(this);
		TextNode.create(1360, 410).text(Text.create("Hello " + this.name.get(), info)).attach(this);

		RectNode
		.create(160, 560, 195, 60)
		.color(UIDemoSignal.PLACEHOLDER)
		.onClick((node, mouseX, mouseY, clickType) -> this.items.add("Item " + (this.items.size() + 1)))
		.body(rect -> TextNode.create(rect.dw(2), rect.dh(2)).text(Text.create("Add", button, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(rect))
		.attach(this);
		RectNode
		.create(365, 560, 195, 60)
		.color(UIDemoSignal.PLACEHOLDER)
		.onClick((node, mouseX, mouseY, clickType) -> this.items.remove(this.items.size() - 1))
		.visible(!this.items.isEmpty())
		.body(rect -> TextNode.create(rect.dw(2), rect.dh(2)).text(Text.create("Remove", button, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(rect))
		.attach(this);
		FlexNode
		.vertical(160, 640, 400)
		.margin(10D)
		.watch(this.items, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY)
		.body(flex -> {
			for (final String item : this.items.get()) {
				RectNode
				.create(0, 0, 400, 40)
				.color(UIDemoSignal.PLACEHOLDER)
				.body(rect -> TextNode.create(10, rect.dh(2)).text(Text.create(item, button, Align.START, Align.CENTER)).anchorY(Align.CENTER).attach(rect))
				.attach(flex);
			}
		})
		.attach(this);

		RectNode
		.create(760, 560, 400, 160)
		.color(UIDemoSignal.PLACEHOLDER)
		.wait(this.cards)
		.skeleton(rect -> RectNode.create(0, 0, rect.getWidth(), rect.getHeight()).color(Color.LOADING))
		.body(rect -> {
			TextNode.create(20, 20).text(Text.create(this.cards.isEmpty() ? "" : this.cards.get(0), button)).attach(rect);
			TextNode.create(20, 70).text(Text.create(this.cards.isEmpty() ? "" : this.cards.get(1), button)).attach(rect);
			TextNode.create(20, 120).text(Text.create(this.cards.isEmpty() ? "" : this.cards.get(2), button)).attach(rect);
		})
		.attach(this);

		super.schedule(() -> this.cards.set(Arrays.asList("Ada Lovelace", "Analytical Engine", "1843")), 3000L);
	}

}