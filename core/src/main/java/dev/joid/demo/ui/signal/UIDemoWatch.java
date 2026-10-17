package dev.joid.demo.ui.signal;

import java.util.concurrent.CompletableFuture;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.node.impl.control.checkbox.DemoCheckboxNode;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.ComputedSignal;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.SignalListener;
import dev.joid.lib.signal.impl.iterable.MapSignal;
import dev.joid.lib.signal.impl.iterable.SetSignal;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.node.property.WatchProperty;
import dev.joid.lib.ui.task.Task;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoWatch extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoWatch.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final IntegerSignal ticks = IntegerSignal.create(0);
		final IntegerSignal watched = IntegerSignal.create(0);
		final BooleanSignal live = BooleanSignal.create(true);
		final IntegerSignal squares = IntegerSignal.create(2);
		final IntegerSignal grown = IntegerSignal.create(0);
		final IntegerSignal mounts = IntegerSignal.create(0);
		final long shownAt = BridgeHandler.CLOCK.get().currentTimeMillis() + 2000L;
		final BooleanSignal ready = BooleanSignal.create(false);
		final CompletableFuture<String> future = new CompletableFuture<>();
		final Signal<String> result = Signal.create(null);
		final IntegerSignal left = IntegerSignal.create(0);
		final IntegerSignal right = IntegerSignal.create(0);
		final ComputedSignal<Integer> sum = Signal.from(() -> left.get() + right.get());
		final IntegerSignal updates = IntegerSignal.create(0);
		final IntegerSignal quiet = IntegerSignal.create(0);
		final IntegerSignal notified = IntegerSignal.create(0);
		final IntegerSignal read = IntegerSignal.create(0);
		final IntegerSignal reset = IntegerSignal.create(5);
		final IntegerSignal adds = IntegerSignal.create(0);
		final MapSignal<String, Integer> counts = MapSignal.create();
		final SetSignal<String> keys = SetSignal.create();
		final IntegerSignal sent = IntegerSignal.create(0);
		final IntegerSignal received = IntegerSignal.create(0);
		final BooleanSignal listening = BooleanSignal.create(true);
		final SignalListener<Integer> listener = value -> received.increment();

		sum.onChange(value -> updates.increment());
		quiet.onChange(value -> notified.increment());
		sent.onChange(listener);
		future.thenAccept(result::set);

		RectNode
		.create(100, 40, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick(event -> ticks.increment())
			.label(Text.create("+1", label))
			.attach(rect);
			RectNode
			.create(40, 120, 320, 40)
			.color(Color.WHITE)
			.watch(ticks)
			.onWatch(event -> watched.increment())
			.attach(rect);
			TextNode.create(40, 185).text(Text.create("Watched: " + watched.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("onWatch", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 40, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode.create(40, 40, 50, 50).checked(live).attach(rect);
			TextNode.create(105, 52).text(Text.create(live.get() ? "Live" : "Paused", info)).attach(rect);
			RectNode
			.create(240, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick(event -> squares.set(squares.peek() % 6 + 1))
			.label(Text.create("+1", label))
			.attach(rect);
			FlexNode
			.horizontal(40, 130, 50)
			.margin(10D)
			.watch(squares, live::get, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY)
			.body(flex -> {
				for (int i = 0; i < squares.peek(); i++) {
					RectNode.create(0, 0, 40, 50).color(UIDemoWatch.INK).attach(flex);
				}
			})
			.attach(rect);
			TextNode.create(40, 200).text(Text.create("Count: " + squares.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Watch while live", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 40, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick(event -> grown.set((grown.peek() + 1) % 5))
			.label(Text.create("Grow", label))
			.attach(rect);
			RectNode
			.create(40, 130, 60, 50)
			.color(UIDemoWatch.INK)
			.watch(grown, (node, signal) -> node.width(60D + grown.peek() * 60D))
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Custom property", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 40, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 320, 100)
			.color(Color.WHITE)
			.wait(container -> BridgeHandler.CLOCK.get().currentTimeMillis() >= shownAt)
			.onMount(event -> mounts.increment())
			.body(container -> {
				TextNode.create(20, 35).text(Text.create("Shown after 2 s", info)).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 175).text(Text.create("Mounted: " + mounts.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("wait a delay", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 380, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick(event -> ready.set(true))
			.label(Text.create("Ready", label))
			.attach(rect);
			RectNode
			.create(40, 120, 320, 80)
			.color(Color.WHITE)
			.wait(container -> ready.get())
			.skeleton(container -> RectNode.create(0, 0, container.getWidth(), container.getHeight()).color(UIDemoWatch.PLACEHOLDER).body(skeleton -> {
				TextNode.create(20, 25).text(Text.create("Press Ready", info)).attach(skeleton);
			}))
			.body(container -> {
				TextNode.create(20, 25).text(Text.create("Shown when ready", info)).attach(container);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("wait a condition", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 380, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 320, 100)
			.color(Color.WHITE)
			.wait(container -> result.get() != null)
			.body(container -> {
				TextNode.create(20, 35).text(Text.create("Result: " + result.get(), info)).attach(container);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Future", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 380, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 140, 50)
			.color(UIDemoWatch.INK)
			.onClick(event -> {
				left.increment();
				right.increment();
			})
			.label(Text.create("Two sets", label))
			.attach(rect);
			RectNode
			.create(200, 40, 140, 50)
			.color(UIDemoWatch.INK)
			.onClick(event -> Signal.batch(() -> {
				left.increment();
				right.increment();
			}))
			.label(Text.create("Batch", label))
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Sum: " + sum.get(), info)).attach(rect);
			TextNode.create(40, 170).text(Text.create("Updates: " + updates.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("batch", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 380, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 70, 50)
			.color(UIDemoWatch.INK)
			.onClick(event -> quiet.increment())
			.label(Text.create("+1", label))
			.attach(rect);
			RectNode
			.create(125, 40, 110, 50)
			.color(UIDemoWatch.INK)
			.onClick(event -> quiet.silent(IntegerSignal::increment))
			.label(Text.create("Silent", label))
			.attach(rect);
			RectNode
			.create(250, 40, 125, 50)
			.color(UIDemoWatch.INK)
			.onClick(event -> Signal.silent(quiet::increment))
			.label(Text.create("All silent", label))
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Value: " + quiet.get(), info)).attach(rect);
			TextNode.create(40, 170).text(Text.create("Notified: " + notified.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("silent", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 720, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick(event -> read.increment())
			.label(Text.create("+1", label))
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("get: " + read.get(), info)).attach(rect);
			TextNode.create(40, 170).text(Text.create("peek: " + read.peek(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("peek", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 720, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick(event -> reset.increment())
			.label(Text.create("+1", label))
			.attach(rect);
			RectNode
			.create(180, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick(event -> reset.reset())
			.label(Text.create("Reset", label))
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Value: " + reset.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("reset", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 720, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick(event -> {
				final String key = "Key " + adds.peek() % 3;
				adds.increment();
				counts.put(key, counts.containsKey(key) ? counts.get(key) + 1 : 1);
				keys.add(key);
			})
			.label(Text.create("Add", label))
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Map Key 0: " + (counts.containsKey("Key 0") ? counts.get("Key 0") : 0), info)).attach(rect);
			TextNode.create(40, 170).text(Text.create("Set size: " + keys.size(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Map and set", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 720, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick(event -> sent.increment())
			.label(Text.create("Send", label))
			.attach(rect);
			RectNode
			.create(180, 40, 180, 50)
			.color(UIDemoWatch.INK)
			.onClick(event -> {
				if (listening.peek()) {
					sent.removeOnChange(listener);
				} else {
					sent.onChange(listener);
				}
				listening.toggle();
			})
			.label(Text.create(listening.get() ? "Unsubscribe" : "Subscribe", label))
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Received: " + received.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("unsubscribe", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		Task.create(1500L).onRun(event -> future.complete("Done")).attach(this);
	}

}