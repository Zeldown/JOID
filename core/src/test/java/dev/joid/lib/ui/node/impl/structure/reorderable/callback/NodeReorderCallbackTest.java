package dev.joid.lib.ui.node.impl.structure.reorderable.callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.reorderable.ReorderableFlexNode;
import lombok.NonNull;

public class NodeReorderCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeReorderCallback<ReorderableFlexNode> callback = (node, child) -> received.addAll(Arrays.asList(node, child));
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(0D, 0D, 200D);
		final RectNode child = RectNode.create(0D, 0D, 200D, 50D);
		final DispatchContext context = DispatchContext.create();
		callback.pre(flex, context, child);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(flex, context, child);
		Assert.assertEquals(Arrays.asList(flex, child), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeReorderCallback<ReorderableFlexNode> callback = (node, child) -> received.add(child);
		callback.post(ReorderableFlexNode.vertical(0D, 0D, 200D), DispatchContext.create(true), RectNode.create(0D, 0D, 200D, 50D));
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnlyWhenTheOrderChanges() {
		final List<Object> received = new ArrayList<>();
		final RectNode first = RectNode.create(0D, 0D, 200D, 50D);
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).margin(10D).onReorder((node, child) -> received.addAll(Arrays.asList(node, child)));
		flex.append(first, RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.frames(2);
		Assert.assertTrue(received.isEmpty());
		this.bridges.move(150D, 300D).frames(60);
		Assert.assertEquals(Arrays.asList(flex, first), received);
	}

	@Test
	public void followsTheIndexOfTheDraggedChild() {
		final List<Object> indexes = new ArrayList<>();
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).margin(10D).onReorder((node, child) -> indexes.add(node.getCurrentIndex()));
		flex.append(RectNode.create(0D, 0D, 200D, 50D), RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.frame().move(150D, 300D).frames(60);
		Assert.assertEquals(Arrays.asList(1), indexes);
	}

	@Test
	public void staysQuietOnceReleased() {
		final List<Object> received = new ArrayList<>();
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).margin(10D).onReorder((node, child) -> received.add(child));
		flex.append(RectNode.create(0D, 0D, 200D, 50D), RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(150D, 300D).frames(5);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		final int count = received.size();
		this.bridges.frames(60);
		Assert.assertEquals(count, received.size());
	}

	@Test
	public void skipsTheCallbackWhenThePrePhaseConsumesAMove() {
		final List<Object> received = new ArrayList<>();
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).onReorder(new NodeReorderCallback<ReorderableFlexNode>() {

			@Override
			public void apply(final @NonNull ReorderableFlexNode node, final @NonNull Node child) {
				received.add(child);
			}

			@Override
			@NodeCallbackMethod(Phase.PRE)
			public void pre(final @NonNull ReorderableFlexNode node, final @NonNull DispatchContext context, final @NonNull Node child) {
				context.cancel();
			}

		});
		final RectNode first = RectNode.create(0D, 0D, 200D, 50D);
		final RectNode second = RectNode.create(0D, 0D, 200D, 50D);
		flex.append(first, second);
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(150D, 300D).frames(60);
		Assert.assertTrue(received.isEmpty());
		Assert.assertEquals(Arrays.asList(first, second), flex.getLogicalOrder());
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