package dev.joid.lib.ui.event.impl.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.property.watch.WatchProperty;

public class NodeWatchEventTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void receivesTheValueAndItsProperties() {
		final List<Object> received = new ArrayList<>();
		final Signal<Integer> signal = new Signal<>(1);
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).watch(signal, () -> true, WatchProperty.CLEAR_CHILDREN).onWatch(event -> received.addAll(Arrays.asList(event.getNode(), event.getValue(), event.getProperties())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		Assert.assertTrue(received.isEmpty());
		signal.set(2);
		Assert.assertEquals(Arrays.asList(rect, 2, Collections.singletonList(WatchProperty.CLEAR_CHILDREN)), received);
	}

	@Test
	public void firesOnceThePropertiesAreApplied() {
		final AtomicInteger bodies = new AtomicInteger();
		final List<Object> received = new ArrayList<>();
		final Signal<Integer> signal = new Signal<>(1);
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).body(node -> bodies.incrementAndGet()).watch(signal, () -> true, WatchProperty.BODY).onWatch(event -> received.add(bodies.get()));
		this.bridges.open(new NodeUI(rect)).frames(30);
		signal.set(2);
		Assert.assertEquals(Collections.singletonList(2), received);
	}

	@Test
	public void waitsForTheNodeToJoinAUi() {
		final List<Object> received = new ArrayList<>();
		final Signal<Integer> signal = new Signal<>(1);
		RectNode.create(100D, 100D, 200D, 100D).watch(signal).onWatch(event -> received.add(event.getNode()));
		signal.set(2);
		Assert.assertTrue(received.isEmpty());
	}

	public static final class NodeUI extends UI {

		private final Node[] nodes;

		private NodeUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

	}

}