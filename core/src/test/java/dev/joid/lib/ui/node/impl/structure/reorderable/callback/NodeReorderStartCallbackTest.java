package dev.joid.lib.ui.node.impl.structure.reorderable.callback;

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
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.reorderable.ReorderableFlexNode;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

public class NodeReorderStartCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeReorderStartCallback callback = (node, child) -> received.addAll(Arrays.asList(node, child));
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
		final NodeReorderStartCallback callback = (node, child) -> received.add(child);
		callback.post(ReorderableFlexNode.vertical(0D, 0D, 200D), InternalContext.create(true), RectNode.create(0D, 0D, 200D, 50D));
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnceTheDragStarted() {
		final List<Object> received = new ArrayList<>();
		final RectNode second = RectNode.create(0D, 0D, 200D, 50D);
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).margin(10D).onReorderStart((node, child) -> received.addAll(Arrays.asList(node, child, node.isDragging(child), node.getInitialIndex())));
		flex.append(RectNode.create(0D, 0D, 200D, 50D), second);
		this.bridges.open(new NodeUI(flex)).move(150D, 180D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(Arrays.asList(flex, second, true, 1), received);
		this.bridges.move(150D, 300D).frames(10);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.frames(60);
		Assert.assertEquals(4, received.size());
	}

	@Test
	public void firesForADragStartedFromCode() {
		final List<Object> received = new ArrayList<>();
		final RectNode first = RectNode.create(0D, 0D, 200D, 50D);
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).auto(false).onReorderStart((node, child) -> received.add(child));
		flex.append(first);
		this.bridges.open(new NodeUI(flex));
		flex.startDrag(first);
		Assert.assertEquals(Collections.singletonList(first), received);
	}

	@Test
	public void waitsForALeftPressOverAChild() {
		final List<Object> received = new ArrayList<>();
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).onReorderStart((node, child) -> received.add(child));
		flex.append(RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.RIGHT);
		this.bridges.move(150D, 400D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void runsEveryCallback() {
		final List<Object> received = new ArrayList<>();
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).onReorderStart((node, child) -> received.add("first")).onReorderStart((node, child) -> received.add("second"));
		flex.append(RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(Arrays.asList("first", "second"), received);
	}

	@Test
	public void skipsTheCallbackWhenThePrePhaseConsumesTheStart() {
		final List<Object> received = new ArrayList<>();
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).onReorderStart(new NodeReorderStartCallback() {

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