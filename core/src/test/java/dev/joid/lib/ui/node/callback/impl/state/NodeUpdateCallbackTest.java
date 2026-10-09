package dev.joid.lib.ui.node.callback.impl.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import lombok.NonNull;

public class NodeUpdateCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeUpdateCallback<RectNode> callback = received::add;
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final DispatchContext context = DispatchContext.create();
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
		final NodeUpdateCallback<RectNode> callback = received::add;
		callback.post(RectNode.create(0D, 0D, 10D, 10D), DispatchContext.create(true));
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnEveryUpdate() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onUpdate(received::add);
		this.bridges.open(new NodeUI(rect)).frames(30);
		received.clear();
		this.bridges.frames(3);
		Assert.assertEquals(Arrays.asList(rect, rect, rect), received);
	}

	@Test
	public void firesOnceTheChildrenAreUpdated() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).onUpdate(received::add);
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).onUpdate(received::add);
		parent.append(child);
		this.bridges.open(new NodeUI(parent)).frames(30);
		received.clear();
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList(child, parent), received);
	}

	@Test
	public void skipsTheChildrenWhenThePrePhaseConsumesIt() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).onUpdate(new NodeUpdateCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode node) {
				received.add(node);
			}

			@Override
			@NodeCallbackMethod(Phase.PRE)
			public void pre(final @NonNull RectNode node, final @NonNull DispatchContext context) {
				context.cancel();
			}

		});
		parent.append(RectNode.create(10D, 10D, 20D, 20D).onUpdate(received::add));
		this.bridges.open(new NodeUI(parent)).frames(30);
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