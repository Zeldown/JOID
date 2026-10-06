package dev.joid.lib.ui.node.impl.structure.reorderable.callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.reorderable.ReorderableFlexNode;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

public class NodeReorderCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeReorderCallback callback = (node, child) -> received.addAll(Arrays.asList(node, child));
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(0D, 0D, 200D);
		final RectNode child = RectNode.create(0D, 0D, 200D, 50D);
		final InternalContext context = InternalContext.create();
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
		final NodeReorderCallback callback = (node, child) -> received.add(child);
		callback.post(ReorderableFlexNode.vertical(0D, 0D, 200D), InternalContext.create(true), RectNode.create(0D, 0D, 200D, 50D));
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnEveryFrameOfTheDrag() {
		final List<Object> received = new ArrayList<>();
		final RectNode first = RectNode.create(0D, 0D, 200D, 50D);
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).margin(10D).onReorder((node, child) -> received.addAll(Arrays.asList(node, child)));
		flex.append(first, RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertTrue(received.isEmpty());
		this.bridges.frames(2);
		Assert.assertEquals(Arrays.asList(flex, first, flex, first), received);
	}

	@Test
	public void followsTheIndexOfTheDraggedChild() {
		final List<Object> indexes = new ArrayList<>();
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).margin(10D).onReorder((node, child) -> indexes.add(node.getCurrentIndex()));
		flex.append(RectNode.create(0D, 0D, 200D, 50D), RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.frame().move(150D, 300D).frames(60);
		Assert.assertEquals(0, indexes.get(0));
		Assert.assertEquals(1, indexes.get(indexes.size() - 1));
	}

	@Test
	public void staysQuietOnceReleased() {
		final List<Object> received = new ArrayList<>();
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).margin(10D).onReorder((node, child) -> received.add(child));
		flex.append(RectNode.create(0D, 0D, 200D, 50D), RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.move(150D, 300D).frames(5);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		final int count = received.size();
		this.bridges.frames(60);
		Assert.assertEquals(count, received.size());
	}

	@Test
	public void skipsTheCallbackWhenThePrePhaseConsumesAMove() {
		final List<Object> received = new ArrayList<>();
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).onReorder(new NodeReorderCallback() {

			@Override
			public void apply(final @NonNull ReorderableFlexNode node, final @NonNull Node child) {
				received.add(child);
			}

			@Override
			@NodeCallbackMethod(Type.PRE)
			public void pre(final @NonNull ReorderableFlexNode node, final @NonNull InternalContext context, final @NonNull Node child) {
				context.cancel();
			}

		});
		flex.append(RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.frames(3);
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