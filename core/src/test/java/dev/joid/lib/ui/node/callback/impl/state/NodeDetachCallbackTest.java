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

public class NodeDetachCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeDetachCallback<RectNode> callback = received::add;
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
		final NodeDetachCallback<RectNode> callback = received::add;
		callback.post(RectNode.create(0D, 0D, 10D, 10D), DispatchContext.create(true));
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesWhenTheParentClearsItsChildren() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D);
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).onDetach(received::add);
		parent.append(child);
		this.bridges.open(new NodeUI(parent)).frames(30);
		Assert.assertTrue(received.isEmpty());
		parent.clearChildren();
		Assert.assertEquals(Collections.singletonList(child), received);
	}

	@Test
	public void detachesTheChildrenFirst() {
		final List<Object> received = new ArrayList<>();
		final RectNode root = RectNode.create(100D, 100D, 200D, 100D);
		final RectNode parent = RectNode.create(10D, 10D, 100D, 50D).onDetach(received::add);
		final RectNode child = RectNode.create(5D, 5D, 20D, 20D).onDetach(received::add);
		parent.append(child);
		root.append(parent);
		this.bridges.open(new NodeUI(root)).frames(30);
		root.clearChildren();
		Assert.assertEquals(Arrays.asList(child, parent), received);
	}

	@Test
	public void firesWhenTheUiCloses() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onDetach(received::add);
		final NodeUI ui = new NodeUI(rect);
		this.bridges.open(ui).frames(30);
		ui.dispose();
		Assert.assertEquals(Collections.singletonList(rect), received);
	}

	@Test
	public void keepsTheChildrenAttachedWhenThePrePhaseConsumesIt() {
		final List<Object> received = new ArrayList<>();
		final RectNode root = RectNode.create(100D, 100D, 200D, 100D);
		final RectNode parent = RectNode.create(10D, 10D, 100D, 50D).onDetach(new NodeDetachCallback<RectNode>() {

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
		parent.append(RectNode.create(5D, 5D, 20D, 20D).onDetach(received::add));
		root.append(parent);
		this.bridges.open(new NodeUI(root)).frames(30);
		root.clearChildren();
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