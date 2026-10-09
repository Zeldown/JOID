package dev.joid.lib.ui.node.callback.impl.signal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.property.watch.WatchProperty;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.signal.Signal;
import lombok.NonNull;

public class NodeWatchCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeWatchCallback<RectNode> callback = (node, signal, properties) -> received.addAll(Arrays.asList(node, signal, Arrays.asList(properties)));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final Signal<Integer> signal = Signal.of(1);
		final InternalContext context = InternalContext.create();
		callback.pre(rect, context, signal, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context, signal, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY);
		Assert.assertEquals(Arrays.asList(rect, signal, Arrays.asList(WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY)), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeWatchCallback<RectNode> callback = (node, signal, properties) -> received.add(node);
		callback.post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create(true), Signal.of(1), WatchProperty.BODY);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void receivesTheSignalAndItsProperties() {
		final List<Object> received = new ArrayList<>();
		final Signal<Integer> signal = Signal.of(1);
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).watch(signal, () -> true, WatchProperty.CLEAR_CHILDREN).onWatch((node, watched, properties) -> received.addAll(Arrays.asList(node, watched, Arrays.asList(properties))));
		this.bridges.open(new NodeUI(rect)).frames(30);
		Assert.assertTrue(received.isEmpty());
		signal.set(2);
		Assert.assertEquals(Arrays.asList(rect, signal, Collections.singletonList(WatchProperty.CLEAR_CHILDREN)), received);
	}

	@Test
	public void firesOnceThePropertiesAreApplied() {
		final AtomicInteger bodies = new AtomicInteger();
		final List<Object> received = new ArrayList<>();
		final Signal<Integer> signal = Signal.of(1);
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).body(node -> bodies.incrementAndGet()).watch(signal, () -> true, WatchProperty.BODY).onWatch((node, watched, properties) -> received.add(bodies.get()));
		this.bridges.open(new NodeUI(rect)).frames(30);
		signal.set(2);
		Assert.assertEquals(Collections.singletonList(2), received);
	}

	@Test
	public void waitsForTheNodeToJoinAUi() {
		final List<Object> received = new ArrayList<>();
		final Signal<Integer> signal = Signal.of(1);
		RectNode.create(100D, 100D, 200D, 100D).watch(signal).onWatch((node, watched, properties) -> received.add(node));
		signal.set(2);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void skipsThePropertiesWhenThePrePhaseConsumesIt() {
		final AtomicInteger bodies = new AtomicInteger();
		final List<Object> received = new ArrayList<>();
		final Signal<Integer> signal = Signal.of(1);
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).body(node -> bodies.incrementAndGet()).watch(signal, () -> true, WatchProperty.BODY).onWatch(new NodeWatchCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode node, final @NonNull Signal<?> watched, final @NonNull WatchProperty @NonNull... properties) {
				received.add(node);
			}

			@Override
			@NodeCallbackMethod(Phase.PRE)
			public void pre(final @NonNull RectNode node, final @NonNull InternalContext context, final @NonNull Signal<?> watched, final @NonNull WatchProperty @NonNull... properties) {
				context.cancel();
			}

		});
		this.bridges.open(new NodeUI(rect)).frames(30);
		signal.set(2);
		Assert.assertEquals(1, bodies.get());
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