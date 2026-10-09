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
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.utils.context.InternalContext;

import lombok.NonNull;

public class NodeInitCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeInitCallback<RectNode> callback = received::add;
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
		final NodeInitCallback<RectNode> callback = received::add;
		callback.post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create(true));
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnceTheNodeIsLoadedInAUi() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onInit(node -> received.addAll(Arrays.asList(node, node.hasUi())));
		Assert.assertTrue(received.isEmpty());
		this.bridges.open(new NodeUI(rect)).frames(30);
		Assert.assertEquals(Arrays.asList(rect, true), received);
	}

	@Test
	public void firesForAChildAppendedToALoadedNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D);
		this.bridges.open(new NodeUI(parent)).frames(30);
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).onInit(received::add);
		Assert.assertTrue(received.isEmpty());
		parent.append(child);
		Assert.assertEquals(Collections.singletonList(child), received);
	}

	@Test
	public void keepsTheNodeOutOfTheUiWhenThePrePhaseConsumesIt() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onInit(new NodeInitCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode node) {
				received.add(node);
			}

			@Override
			@NodeCallbackMethod(Phase.PRE)
			public void pre(final @NonNull RectNode node, final @NonNull InternalContext context) {
				context.cancel();
			}

		});
		rect.load(new NodeUI());
		Assert.assertFalse(rect.hasUi());
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void runsEveryInitCallbackOfANode() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onInit(node -> received.add("first")).onInit(node -> received.add("second"));
		this.bridges.open(new NodeUI(rect)).frames(30);
		Assert.assertEquals(Arrays.asList("first", "second"), received);
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