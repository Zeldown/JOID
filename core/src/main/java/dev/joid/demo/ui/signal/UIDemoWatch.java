package dev.joid.demo.ui.signal;

import java.util.concurrent.CompletableFuture;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.node.impl.structure.checkbox.DemoCheckboxNode;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.ComputedSignal;
import dev.joid.lib.signal.ISignalListener;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.impl.iterable.MapSignal;
import dev.joid.lib.signal.impl.iterable.SetSignal;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.ui.node.property.watch.WatchProperty;
import dev.joid.lib.ui.task.Task;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoWatch extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).freeze();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).freeze();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoWatch.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final IntegerSignal ticks = new IntegerSignal(0);
		final IntegerSignal watched = new IntegerSignal(0);
		final BooleanSignal live = new BooleanSignal(true);
		final IntegerSignal squares = new IntegerSignal(2);
		final IntegerSignal grown = new IntegerSignal(0);
		final IntegerSignal mounts = new IntegerSignal(0);
		final long shownAt = BridgeHandler.CLOCK.get().currentTimeMillis() + 2000L;
		final BooleanSignal ready = new BooleanSignal(false);
		final CompletableFuture<String> future = new CompletableFuture<>();
		final Signal<String> result = new Signal<>();
		final IntegerSignal left = new IntegerSignal(0);
		final IntegerSignal right = new IntegerSignal(0);
		final ComputedSignal<Integer> sum = Signal.from(() -> left.get() + right.get());
		final IntegerSignal updates = new IntegerSignal(0);
		final IntegerSignal quiet = new IntegerSignal(0);
		final IntegerSignal notified = new IntegerSignal(0);
		final IntegerSignal read = new IntegerSignal(0);
		final IntegerSignal reset = new IntegerSignal(5);
		final MapSignal<String, Integer> counts = new MapSignal<>();
		final SetSignal<String> keys = new SetSignal<>();
		final IntegerSignal sent = new IntegerSignal(0);
		final IntegerSignal received = new IntegerSignal(0);
		final BooleanSignal listening = new BooleanSignal(true);
		final ISignalListener<Integer> listener = value -> {
			received.increment();
		};

		sum.onChange(value -> {
			updates.increment();
		});
		quiet.onChange(value -> {
			notified.increment();
		});
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
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("+1", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode
			.create(40, 120, 320, 40)
			.color(Color.WHITE)
			.watch(ticks)
			.onWatch(event -> watched.increment())
			.attach(rect);
			TextNode.create(40, 185).text(Text.create("Watched: " + watched.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("onWatch", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
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
			.onClick(event -> squares.set(squares.get() % 6 + 1))
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
			.onClick(event -> grown.set((grown.get() + 1) % 5))
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
			.wait(container -> BridgeHandler.CLOCK.get().currentTimeMillis() >= shownAt)
			.onMount(event -> mounts.increment())
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
			.onClick(event -> ready.set(true))
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Ready", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
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
			.wait(container -> result.get() != null)
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
			.onClick(event -> {
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
			.onClick(event -> Signal.batch(() -> {
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
			.onClick(event -> quiet.increment())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("+1", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode
			.create(200, 40, 140, 50)
			.color(UIDemoWatch.INK)
			.onClick(event -> {
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
			.onClick(event -> read.increment())
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
			.onClick(event -> reset.increment())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("+1", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode
			.create(180, 40, 120, 50)
			.color(UIDemoWatch.INK)
			.onClick(event -> reset.reset())
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
			.onClick(event -> {
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
			.onClick(event -> sent.increment())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Send", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode
			.create(180, 40, 180, 50)
			.color(UIDemoWatch.INK)
			.onClick(event -> {
				if (listening.get()) {
					sent.removeOnChange(listener);
				} else {
					sent.onChange(listener);
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

		Task.create(1500L).onRun(event -> future.complete("Done")).attach(this);
	}

}