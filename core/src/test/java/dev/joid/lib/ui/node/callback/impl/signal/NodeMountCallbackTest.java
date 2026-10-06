package dev.joid.lib.ui.node.callback.impl.signal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.utils.context.InternalContext;

public class NodeMountCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeMountCallback<RectNode> callback = received::add;
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final InternalContext context = InternalContext.create();
		callback.pre(rect, context);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context);
		Assert.assertEquals(Collections.singletonList(rect), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeMountCallback<RectNode> callback = received::add;
		callback.post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create(true));
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnceOnTheFirstDrawnFrame() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onMount(received::add);
		this.bridges.getUi().add(new NodeUI(rect));
		Assert.assertTrue(received.isEmpty());
		this.bridges.frames(30);
		Assert.assertEquals(Collections.singletonList(rect), received);
	}

	@Test
	public void waitsForTheNodeToBeReady() {
		final List<Object> received = new ArrayList<>();
		final AtomicBoolean ready = new AtomicBoolean();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).wait(node -> ready.get()).onMount(received::add);
		this.bridges.open(new NodeUI(rect)).frames(30);
		Assert.assertTrue(received.isEmpty());
		ready.set(true);
		this.bridges.frames(3);
		Assert.assertEquals(Collections.singletonList(rect), received);
	}

	@Test
	public void waitsForItsParentToBeReady() {
		final List<Object> received = new ArrayList<>();
		final AtomicBoolean ready = new AtomicBoolean();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).wait(node -> ready.get());
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).onMount(received::add);
		parent.append(child);
		this.bridges.open(new NodeUI(parent)).frames(30);
		Assert.assertTrue(received.isEmpty());
		ready.set(true);
		this.bridges.frames(3);
		Assert.assertEquals(Collections.singletonList(child), received);
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