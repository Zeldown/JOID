package dev.joid.lib.ui.node.callback.impl.state;

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
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

public class NodeReloadCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeReloadCallback<RectNode> callback = received::add;
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
		final NodeReloadCallback<RectNode> callback = received::add;
		callback.post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create(true));
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesForTheChildrenBeforeTheNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).onReload(received::add);
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).onReload(received::add);
		parent.append(child);
		this.bridges.open(new NodeUI(parent)).frames(30);
		Assert.assertTrue(received.isEmpty());
		parent.reload();
		Assert.assertEquals(Arrays.asList(child, parent), received);
	}

	@Test
	public void firesOnceTheNodeIsLoadedAgain() {
		final AtomicInteger loads = new AtomicInteger();
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onInit(node -> loads.incrementAndGet()).onReload(node -> received.add(loads.get()));
		this.bridges.open(new NodeUI(rect)).frames(30);
		rect.reload();
		Assert.assertEquals(Collections.singletonList(2), received);
	}

	@Test
	public void keepsTheNodeAsItIsWhenThePrePhaseConsumesIt() {
		final AtomicInteger loads = new AtomicInteger();
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).onInit(node -> loads.incrementAndGet()).onReload(new NodeReloadCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode node) {
				received.add(node);
			}

			@Override
			@NodeCallbackMethod(Type.PRE)
			public void pre(final @NonNull RectNode node, final @NonNull InternalContext context) {
				context.cancel();
			}

		});
		parent.append(RectNode.create(10D, 10D, 20D, 20D).onReload(received::add));
		this.bridges.open(new NodeUI(parent)).frames(30);
		parent.reload();
		Assert.assertTrue(received.isEmpty());
		Assert.assertEquals(1, loads.get());
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