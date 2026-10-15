package dev.joid.lib.ui.event;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeKeyEvent;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class EventBusTest {

	private static final EventType<NodeKeyEvent<RectNode>>                 KEY    = EventType.create("handleKeyPressed");
	private static final EventType<NodeChangeEvent.Pre<RectNode, Integer>> CHANGE = EventType.create("beforeChange");

	private final EventBus     bus   = EventBus.create(() -> "RectNode in TestUI");
	private final List<String> trace = new ArrayList<>();

	@Test
	public void runsItsListenersByPriorityThenByOrder() {
		this.bus.listen(EventBusTest.KEY, EventListener.priority(EventPriority.LOW, event -> this.trace.add("low")));
		this.bus.listen(EventBusTest.KEY, EventListener.priority(EventPriority.MONITOR, event -> this.trace.add("monitor")));
		this.bus.listen(EventBusTest.KEY, event -> this.trace.add("first normal"));
		this.bus.listen(EventBusTest.KEY, EventListener.priority(EventPriority.HIGHEST, event -> this.trace.add("highest")));
		this.bus.listen(EventBusTest.KEY, event -> this.trace.add("second normal"));
		this.bus.listen(EventBusTest.KEY, EventListener.priority(EventPriority.LOWEST, event -> this.trace.add("lowest")));
		this.bus.listen(EventBusTest.KEY, EventListener.priority(EventPriority.HIGH, event -> this.trace.add("high")));
		this.bus.fire(EventBusTest.KEY, this.key());
		Assert.assertEquals(Arrays.asList("highest", "high", "first normal", "second normal", "low", "lowest", "monitor"), this.trace);
	}

	@Test
	public void skipsTheNextListenersOfAConsumedEventButItsMonitors() {
		final EventListener<NodeKeyEvent<RectNode>> consume = input -> {
			this.trace.add("high");
			input.consume();
		};
		final EventListener<NodeKeyEvent<RectNode>> monitor = input -> this.trace.add("monitor " + input.isConsumed());
		this.bus.listen(EventBusTest.KEY, EventListener.priority(EventPriority.HIGH, consume));
		this.bus.listen(EventBusTest.KEY, input -> this.trace.add("normal"));
		this.bus.listen(EventBusTest.KEY, EventListener.priority(EventPriority.MONITOR, monitor));
		this.bus.fire(EventBusTest.KEY, this.key());
		Assert.assertEquals(Arrays.asList("high", "monitor true"), this.trace);
	}

	@Test
	public void skipsTheNextListenersOfACancelledEventButItsMonitors() {
		final NodeChangeEvent.Pre<RectNode, Integer> event = NodeChangeEvent.Pre.create(RectNode.create(0D, 0D, 10D, 10D), 1, 2);
		final EventListener<NodeChangeEvent.Pre<RectNode, Integer>> cancel = change -> {
			this.trace.add("high");
			change.cancel();
		};
		final EventListener<NodeChangeEvent.Pre<RectNode, Integer>> monitor = change -> this.trace.add("monitor " + change.isCancelled());
		this.bus.listen(EventBusTest.CHANGE, change -> this.trace.add("normal"));
		this.bus.listen(EventBusTest.CHANGE, EventListener.priority(EventPriority.HIGH, cancel));
		this.bus.listen(EventBusTest.CHANGE, EventListener.priority(EventPriority.MONITOR, monitor));
		this.bus.fire(EventBusTest.CHANGE, event);
		Assert.assertEquals(Arrays.asList("high", "monitor true"), this.trace);
		Assert.assertTrue(event.isCancelled());
	}

	@Test
	public void refusesAMonitorThatConsumesOrCancels() {
		final NodeKeyEvent<RectNode> key = this.key();
		final NodeChangeEvent.Pre<RectNode, Integer> change = NodeChangeEvent.Pre.create(RectNode.create(0D, 0D, 10D, 10D), 1, 2);
		final EventListener<NodeKeyEvent<RectNode>> consume = event -> event.consume();
		final EventListener<NodeChangeEvent.Pre<RectNode, Integer>> cancel = event -> event.cancel();
		this.bus.listen(EventBusTest.KEY, EventListener.priority(EventPriority.MONITOR, consume));
		this.bus.listen(EventBusTest.CHANGE, EventListener.priority(EventPriority.MONITOR, cancel));
		final String printed = EventBusTest.capture(() -> {
			this.bus.fire(EventBusTest.KEY, key);
			this.bus.fire(EventBusTest.CHANGE, change);
		});
		Assert.assertFalse(key.isConsumed());
		Assert.assertFalse(change.isCancelled());
		Assert.assertTrue(printed, printed.contains("[JOID] The handleKeyPressed listener of RectNode in TestUI failed (java.lang.IllegalStateException: A MONITOR listener only reads NodeKeyEvent, it cannot call consume(): listen with another priority to consume it"));
		Assert.assertTrue(printed, printed.contains("[JOID] The beforeChange listener of RectNode in TestUI failed (java.lang.IllegalStateException: A MONITOR listener only reads Pre, it cannot call cancel(): listen with another priority to cancel it"));
	}

	@Test
	public void letsTheEventBeConsumedAgainOnceItsMonitorsRan() {
		final NodeKeyEvent<RectNode> key = this.key();
		this.bus.listen(EventBusTest.KEY, EventListener.priority(EventPriority.MONITOR, event -> this.trace.add("monitor")));
		this.bus.fire(EventBusTest.KEY, key);
		key.consume();
		Assert.assertTrue(key.isConsumed());
	}

	@Test
	public void keepsRunningItsListenersAfterAFailure() {
		this.bus.listen(EventBusTest.KEY, EventListener.priority(EventPriority.HIGH, event -> {
			throw new IllegalArgumentException("broken");
		}));
		this.bus.listen(EventBusTest.KEY, event -> this.trace.add("next"));
		final String printed = EventBusTest.capture(() -> this.bus.fire(EventBusTest.KEY, this.key()));
		Assert.assertEquals(Collections.singletonList("next"), this.trace);
		Assert.assertTrue(printed, printed.startsWith("[JOID] The handleKeyPressed listener of RectNode in TestUI failed (java.lang.IllegalArgumentException: broken"));
	}

	@Test
	public void reportsTheSameFailureOfAListenerOnce() {
		this.bus.listen(EventBusTest.KEY, event -> {
			throw new IllegalArgumentException("broken");
		});
		final String printed = EventBusTest.capture(() -> {
			this.bus.fire(EventBusTest.KEY, this.key());
			this.bus.fire(EventBusTest.KEY, this.key());
		});
		Assert.assertEquals(printed.indexOf("[JOID]"), printed.lastIndexOf("[JOID]"));
	}

	@Test
	public void doesNotCancelAChangeWhenABeforeListenerFails() {
		final NodeChangeEvent.Pre<RectNode, Integer> change = NodeChangeEvent.Pre.create(RectNode.create(0D, 0D, 10D, 10D), 1, 2);
		this.bus.listen(EventBusTest.CHANGE, event -> {
			throw new IllegalStateException("broken");
		});
		EventBusTest.capture(() -> this.bus.fire(EventBusTest.CHANGE, change));
		Assert.assertFalse(change.isCancelled());
	}

	@Test
	public void runsTheListenersFromACopyOfItsList() {
		this.bus.listen(EventBusTest.KEY, event -> {
			this.trace.add("first");
			this.bus.listen(EventBusTest.KEY, added -> this.trace.add("added"));
		});
		this.bus.fire(EventBusTest.KEY, this.key());
		Assert.assertEquals(Collections.singletonList("first"), this.trace);
		Assert.assertEquals(2, this.bus.count(EventBusTest.KEY));
	}

	@Test
	public void keepsItsKeptListenersOnReset() {
		this.bus.listen(EventBusTest.KEY, event -> this.trace.add("kept"));
		this.bus.keep();
		this.bus.listen(EventBusTest.KEY, EventListener.priority(EventPriority.HIGH, event -> this.trace.add("dropped")));
		this.bus.listen(EventBusTest.CHANGE, event -> this.trace.add("dropped"));
		this.bus.reset();
		this.bus.fire(EventBusTest.KEY, this.key());
		Assert.assertEquals(Collections.singletonList("kept"), this.trace);
		Assert.assertFalse(this.bus.has(EventBusTest.CHANGE));
		Assert.assertTrue(this.bus.clear().isEmpty());
	}

	@Test
	public void copiesTheListenersOfAnotherBus() {
		final EventBus other = EventBus.create(() -> "other");
		other.listen(EventBusTest.KEY, event -> this.trace.add("copied"));
		this.bus.addAll(other);
		this.bus.fire(EventBusTest.KEY, this.key());
		Assert.assertEquals(Collections.singletonList("copied"), this.trace);
		Assert.assertEquals(Collections.singleton(EventBusTest.KEY), this.bus.getTypes());
		Assert.assertEquals(0, this.bus.count(EventBusTest.CHANGE));
	}

	private NodeKeyEvent<RectNode> key() {
		return NodeKeyEvent.create(RectNode.create(0D, 0D, 10D, 10D), Key.A, Key.A, null);
	}

	private static String capture(final Runnable runnable) {
		final PrintStream error = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setErr(new PrintStream(output, true));
			runnable.run();
		} finally {
			System.setErr(error);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

}