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

public class NodeReorderEndCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeReorderEndCallback callback = (node, child, oldIndex, newIndex) -> received.addAll(Arrays.asList(node, child, oldIndex, newIndex));
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(0D, 0D, 200D);
		final RectNode child = RectNode.create(0D, 0D, 200D, 50D);
		final InternalContext context = InternalContext.create();
		callback.pre(flex, context, child, 0, 2);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(flex, context, child, 0, 2);
		Assert.assertEquals(Arrays.asList(flex, child, 0, 2), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeReorderEndCallback callback = (node, child, oldIndex, newIndex) -> received.add(child);
		callback.post(ReorderableFlexNode.vertical(0D, 0D, 200D), InternalContext.create(true), RectNode.create(0D, 0D, 200D, 50D), 0, 1);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnceTheChildSettledOnItsSlot() {
		final List<Object> received = new ArrayList<>();
		final RectNode first = RectNode.create(0D, 0D, 200D, 50D);
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).margin(10D).onReorderEnd((node, child, oldIndex, newIndex) -> received.addAll(Arrays.asList(node, child, oldIndex, newIndex, node.getChildIndex(child), child.getY(), node.isDragging(child))));
		flex.append(first, RectNode.create(0D, 0D, 200D, 50D), RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.move(150D, 1000D).frames(60);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		Assert.assertTrue(received.isEmpty());
		this.bridges.frames(60);
		Assert.assertEquals(Arrays.asList(flex, first, 0, 2, 2, 120D, false), received);
	}

	@Test
	public void reportsTheSameIndexForADropInPlace() {
		final List<Object> received = new ArrayList<>();
		final RectNode second = RectNode.create(0D, 0D, 200D, 50D);
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).margin(10D).onReorderEnd((node, child, oldIndex, newIndex) -> received.addAll(Arrays.asList(child, oldIndex, newIndex)));
		flex.append(RectNode.create(0D, 0D, 200D, 50D), second);
		this.bridges.open(new NodeUI(flex)).move(150D, 180D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.frames(10);
		Assert.assertEquals(Arrays.asList(second, 1, 1), received);
	}

	@Test
	public void skipsTheCallbackWhenThePrePhaseConsumesTheEnd() {
		final List<Object> received = new ArrayList<>();
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).onReorderEnd(new NodeReorderEndCallback() {

			@Override
			public void apply(final @NonNull ReorderableFlexNode node, final @NonNull Node child, final int oldIndex, final int newIndex) {
				received.add(child);
			}

			@Override
			@NodeCallbackMethod(Type.PRE)
			public void pre(final @NonNull ReorderableFlexNode node, final @NonNull InternalContext context, final @NonNull Node child, final int oldIndex, final int newIndex) {
				context.cancel();
			}

		});
		flex.append(RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.frames(10);
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