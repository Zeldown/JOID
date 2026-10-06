package dev.joid.lib.ui.node.callback.impl.scroll;

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
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.utils.context.InternalContext;

public class NodeScrollEndCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeScrollEndCallback<RectNode> callback = (node, scrollX, scrollY) -> received.addAll(Arrays.asList(node, scrollX, scrollY));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final InternalContext context = InternalContext.create();
		callback.pre(rect, context, 0D, -700D);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context, 0D, -700D);
		Assert.assertEquals(Arrays.asList(rect, 0D, -700D), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeScrollEndCallback<RectNode> callback = (node, scrollX, scrollY) -> received.add(node);
		callback.post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create(true), 0D, -700D);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnceTheScrollReachesTheEnd() {
		final List<Object> received = new ArrayList<>();
		final ContainerNode box = NodeScrollEndCallbackTest.box(1000D).onScrollEnd((node, scrollX, scrollY) -> received.add(node));
		this.bridges.open(new NodeUI(box)).frames(30);
		box.scrollOffsetY(-200D);
		this.bridges.frames(200);
		Assert.assertTrue(received.isEmpty());
		box.scrollOffsetY(-700D);
		box.scrollOffsetY(-900D);
		Assert.assertTrue(received.isEmpty());
		this.bridges.frames(200);
		Assert.assertEquals(Collections.singletonList(box), received);
	}

	@Test
	public void firesWhenTheWheelReachesTheEnd() {
		final List<Object> received = new ArrayList<>();
		final ContainerNode box = NodeScrollEndCallbackTest.box(360D).onScrollEnd((node, scrollX, scrollY) -> received.add(node));
		this.bridges.open(new NodeUI(box)).frames(30);
		this.bridges.move(700D, 200D).frames(2).scroll(-120).frames(200);
		Assert.assertTrue(received.isEmpty());
		this.bridges.frame().scroll(-120);
		this.bridges.frame().scroll(-120).frames(200);
		Assert.assertEquals(Collections.singletonList(box), received);
	}

	@Test
	public void ignoresAScrollBackToTheTop() {
		final List<Object> received = new ArrayList<>();
		final ContainerNode box = NodeScrollEndCallbackTest.box(1000D).onScrollEnd((node, scrollX, scrollY) -> received.add(node));
		this.bridges.open(new NodeUI(box)).frames(30);
		box.scrollOffsetY(-200D);
		box.scrollOffsetY(0D);
		this.bridges.frames(200);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void ignoresAnEndLeftBeforeTheScrollRests() {
		final List<Object> received = new ArrayList<>();
		final ContainerNode box = NodeScrollEndCallbackTest.box(1000D).onScrollEnd((node, scrollX, scrollY) -> received.add(node));
		this.bridges.open(new NodeUI(box)).frames(30);
		box.scrollOffsetY(-900D);
		this.bridges.frames(5);
		box.scrollOffsetY(-100D);
		this.bridges.frames(200);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void receivesThePositionWhereTheScrollRests() {
		final List<Double> received = new ArrayList<>();
		final ContainerNode box = ContainerNode.create(500D, 100D, 300D, 300D).overflow(OverflowProperty.SCROLL).onScrollEnd((node, scrollX, scrollY) -> received.add(scrollY));
		RectNode.create(0D, 0D, 100D, 1000D).attach(box);
		this.bridges.open(new NodeUI(box)).frames(30);
		box.scrollOffsetY(-700D);
		this.bridges.frames(200);
		Assert.assertEquals(1, received.size());
		Assert.assertEquals(box.getScrollY(), received.get(0), 0D);
	}

	private static ContainerNode box(final double contentHeight) {
		final ContainerNode box = ContainerNode.create(500D, 100D, 300D, 300D).overflow(OverflowProperty.SCROLL);
		RectNode.create(0D, 0D, 100D, contentHeight).attach(box);
		return box;
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