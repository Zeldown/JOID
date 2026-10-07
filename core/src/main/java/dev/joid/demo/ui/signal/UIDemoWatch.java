package dev.joid.demo.ui.signal;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.checkbox.node.DemoCheckboxNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.ui.node.property.watch.WatchProperty;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.signal.ComputedSignal;
import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.SignalSubscriber;
import dev.joid.lib.utils.signal.impl.iterable.MapSignal;
import dev.joid.lib.utils.signal.impl.iterable.SetSignal;
import dev.joid.lib.utils.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;

public class UIDemoWatch extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoWatch.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final IntegerSignal ticks = IntegerSignal.of(0);
		final IntegerSignal watched = IntegerSignal.of(0);
		final BooleanSignal live = BooleanSignal.of(true);
		final IntegerSignal squares = IntegerSignal.of(2);
		final IntegerSignal grown = IntegerSignal.of(0);
		final IntegerSignal mounts = IntegerSignal.of(0);
		final BooleanSignal ready = BooleanSignal.of(false);
		final CompletableFuture<String> future = new CompletableFuture<>();
		final Signal<String> result = Signal.of(future);
		final IntegerSignal left = IntegerSignal.of(0);
		final IntegerSignal right = IntegerSignal.of(0);
		final ComputedSignal<Integer> sum = Signal.from(() -> left.get() + right.get());
		final IntegerSignal updates = IntegerSignal.of(0);
		final IntegerSignal quiet = IntegerSignal.of(0);
		final IntegerSignal notified = IntegerSignal.of(0);
		final IntegerSignal read = IntegerSignal.of(0);
		final IntegerSignal reset = IntegerSignal.of(5);
		final MapSignal<String, Integer> counts = new MapSignal<>(Collections.emptyMap());
		final SetSignal<String> keys = new SetSignal<>(Collections.emptySet());
		final IntegerSignal sent = IntegerSignal.of(0);
		final IntegerSignal received = IntegerSignal.of(0);
		final BooleanSignal listening = BooleanSignal.of(true);
		final SignalSubscriber<Integer> listener = value -> {
			received.increment();
			return true;
		};

		sum.subscribe(value -> {
			updates.increment();
			return true;
		});
		quiet.subscribe(value -> {
			notified.increment();
			return true;
		});
		sent.subscribe(listener);

		RectNode
		.create(100, 40, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick((node, mouseX, mouseY, clickType) -> ticks.increment())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("+1", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode
			.create(40, 120, 320, 40)
			.color(Color.WHITE)
			.watch(ticks)
			.onWatch((node, signal, properties) -> watched.increment())
			.attach(rect);
			TextNode.create(40, 185).text(Text.create("Watched: " + watched.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("onWatch", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 40, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode.create(40, 40, 50, 50).signal(live).attach(rect);
			TextNode.create(105, 52).text(Text.create(live.get() ? "Live" : "Paused", info)).attach(rect);
			RectNode
			.create(240, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick((node, mouseX, mouseY, clickType) -> squares.set(squares.get() % 6 + 1))
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("+1", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			FlexNode
			.horizontal(40, 130, 50)
			.margin(10D)
			.watch(squares, () -> live.get(), WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY)
			.body(flex -> {
				for (int i = 0; i < squares.peek(); i++) {
					RectNode.create(0, 0, 40, 50).color(UIDemoWatch.INK).attach(flex);
				}
			})
			.attach(rect);
			TextNode.create(40, 200).text(Text.create("Count: " + squares.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Watch while live", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 40, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick((node, mouseX, mouseY, clickType) -> grown.set((grown.get() + 1) % 5))
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Grow", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode
			.create(40, 130, 60, 50)
			.color(UIDemoWatch.INK)
			.watch(grown, WatchProperty.custom((node, signal) -> node.width(60D + grown.peek() * 60D)))
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Custom property", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 40, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 320, 100)
			.color(Color.WHITE)
			.wait(2L, TimeUnit.SECONDS)
			.skeleton(container -> RectNode.create(0, 0, container.getWidth(), container.getHeight()).color(Color.LOADING))
			.onMount(container -> mounts.increment())
			.body(container -> {
				TextNode.create(20, 35).text(Text.create("Shown after 2 s", info)).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 175).text(Text.create("Mounted: " + mounts.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("wait a delay", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 380, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick((node, mouseX, mouseY, clickType) -> ready.set(true))
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Ready", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode
			.create(40, 120, 320, 80)
			.color(Color.WHITE)
			.wait(node -> ready.get())
			.skeleton(container -> RectNode.create(0, 0, container.getWidth(), container.getHeight()).color(Color.LOADING))
			.body(container -> {
				TextNode.create(20, 25).text(Text.create("Shown when ready", info)).attach(container);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("wait a condition", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 380, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 320, 100)
			.color(Color.WHITE)
			.wait(result)
			.skeleton(container -> RectNode.create(0, 0, container.getWidth(), container.getHeight()).color(Color.LOADING))
			.body(container -> {
				TextNode.create(20, 35).text(Text.create("Result: " + result.get(), info)).attach(container);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Future", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 380, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 140, 50)
			.color(UIDemoWatch.INK)
			.onClick((node, mouseX, mouseY, clickType) -> {
				left.increment();
				right.increment();
			})
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Two sets", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode
			.create(200, 40, 140, 50)
			.color(UIDemoWatch.INK)
			.onClick((node, mouseX, mouseY, clickType) -> Signal.batch(() -> {
				left.increment();
				right.increment();
			}))
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Batch", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Sum: " + sum.get(), info)).attach(rect);
			TextNode.create(40, 170).text(Text.create("Updates: " + updates.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("batch", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 380, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 140, 50)
			.color(UIDemoWatch.INK)
			.onClick((node, mouseX, mouseY, clickType) -> quiet.increment())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("+1", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode
			.create(200, 40, 140, 50)
			.color(UIDemoWatch.INK)
			.onClick((node, mouseX, mouseY, clickType) -> {
				quiet.silent();
				quiet.increment();
			})
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Silent +1", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Value: " + quiet.get(), info)).attach(rect);
			TextNode.create(40, 170).text(Text.create("Notified: " + notified.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("silent", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 720, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick((node, mouseX, mouseY, clickType) -> read.increment())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("+1", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("get: " + read.get(), info)).attach(rect);
			TextNode.create(40, 170).text(Text.create("peek: " + read.peek(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("peek", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 720, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick((node, mouseX, mouseY, clickType) -> reset.increment())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("+1", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode
			.create(180, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick((node, mouseX, mouseY, clickType) -> reset.reset())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Reset", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Value: " + reset.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("reset", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 720, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick((node, mouseX, mouseY, clickType) -> {
				final String key = "Key " + counts.size() % 3;
				counts.put(key, counts.containsKey(key) ? counts.get(key) + 1 : 1);
				keys.add(key);
			})
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Add", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Map keys: " + counts.size(), info)).attach(rect);
			TextNode.create(40, 170).text(Text.create("Set size: " + keys.size(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Map and set", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 720, 400, 260)
		.color(UIDemoWatch.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick((node, mouseX, mouseY, clickType) -> sent.increment())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Send", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode
			.create(180, 40, 180, 50)
			.color(UIDemoWatch.INK)
			.onClick((node, mouseX, mouseY, clickType) -> {
				if (listening.get()) {
					sent.unsubscribe(listener);
				} else {
					sent.subscribe(listener);
				}
				listening.toggle();
			})
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create(listening.get() ? "Unsubscribe" : "Subscribe", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Received: " + received.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("unsubscribe", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		super.schedule(() -> future.complete("Done"), 1500L);
	}

}