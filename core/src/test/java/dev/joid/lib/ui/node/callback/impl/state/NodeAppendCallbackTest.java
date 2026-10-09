package dev.joid.lib.ui.node.callback.impl.state;

import java.util.ArrayList;
import java.util.Arrays;
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

public class NodeAppendCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeAppendCallback<RectNode> callback = (node, child) -> received.addAll(Arrays.asList(node, child));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final RectNode child = RectNode.create(0D, 0D, 5D, 5D);
		final DispatchContext context = DispatchContext.create();
		callback.pre(rect, context, child);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context, child);
		Assert.assertEquals(Arrays.asList(rect, child), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeAppendCallback<RectNode> callback = (node, child) -> received.add(node);
		callback.post(RectNode.create(0D, 0D, 10D, 10D), DispatchContext.create(true), RectNode.create(0D, 0D, 5D, 5D));
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesForEachAppendedChild() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).onAppend((node, child) -> received.addAll(Arrays.asList(node, child)));
		final RectNode first = RectNode.create(10D, 10D, 20D, 20D);
		final RectNode second = RectNode.create(40D, 10D, 20D, 20D);
		parent.append(first, second);
		final RectNode third = RectNode.create(70D, 10D, 20D, 20D).attach(parent);
		Assert.assertEquals(Arrays.asList(parent, first, parent, second, parent, third), received);
	}

	@Test
	public void firesOnceTheChildIsAttachedAndLoaded() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).onAppend((node, child) -> received.addAll(Arrays.asList(child.getParent() == node, node.getChildren().contains(child), child.hasUi())));
		this.bridges.open(new NodeUI(parent)).frames(30);
		parent.append(RectNode.create(10D, 10D, 20D, 20D));
		Assert.assertEquals(Arrays.asList(true, true, true), received);
	}

	@Test
	public void refusesTheChildWhenThePrePhaseConsumesIt() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).onAppend(new NodeAppendCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode node, final @NonNull Node child) {
				received.add(child);
			}

			@Override
			@NodeCallbackMethod(Phase.PRE)
			public void pre(final @NonNull RectNode node, final @NonNull DispatchContext context, final @NonNull Node child) {
				context.cancel();
			}

		});
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D);
		parent.append(child);
		Assert.assertTrue(parent.getChildren().isEmpty());
		Assert.assertNull(child.getParent());
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