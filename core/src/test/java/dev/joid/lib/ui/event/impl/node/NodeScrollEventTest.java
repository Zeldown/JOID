package dev.joid.lib.ui.event.impl.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.impl.node.NodeScrollEvent.ScrollAxis;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;

public class NodeScrollEventTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void receivesTheRequestedOffsetOnceTheTargetMoved() {
		final List<Object> received = new ArrayList<>();
		final ContainerNode box = NodeScrollEventTest.box(1000D).onScroll(event -> received.addAll(Arrays.asList(event.getNode(), event.getOffset(), event.getNode().getTargetScrollY())));
		this.bridges.open(new NodeUI(box)).frames(30);
		box.scrollOffsetY(-200D);
		box.scrollRatioY(0.5F);
		Assert.assertEquals(Arrays.asList(box, -200D, -200D, box, -350D, -350D), received);
	}

	@Test
	public void firesOnEachWheelTick() {
		final List<Object> received = new ArrayList<>();
		final ContainerNode box = NodeScrollEventTest.box(1000D).onScroll(event -> received.add(event.getOffset()));
		this.bridges.open(new NodeUI(box)).frames(30);
		this.bridges.move(700D, 200D).frames(2).scroll(-1D);
		this.bridges.frame().scroll(-1D);
		Assert.assertEquals(Arrays.asList(-30D, -60D), received);
	}

	@Test
	public void givesTheAxisOfEachScroll() {
		final List<Object> received = new ArrayList<>();
		final ContainerNode box = ContainerNode.create(500D, 100D, 300D, 300D).overflow(OverflowProperty.SCROLL).onScroll(event -> received.addAll(Arrays.asList(event.getAxis(), event.getOffset())));
		RectNode.create(0D, 0D, 1000D, 1000D).attach(box);
		this.bridges.open(new NodeUI(box)).frames(30);
		box.scrollOffsetX(-100D);
		box.scrollOffsetY(-200D);
		Assert.assertEquals(Arrays.asList(ScrollAxis.HORIZONTAL, -100D, ScrollAxis.VERTICAL, -200D), received);
	}

	@Test
	public void ignoresAnOffsetThatMovesNothing() {
		final List<Object> received = new ArrayList<>();
		final ContainerNode box = NodeScrollEventTest.box(1000D).onScroll(event -> received.add(event.getOffset()));
		this.bridges.open(new NodeUI(box)).frames(30);
		box.scrollOffsetY(-200D);
		box.scrollOffsetY(-200D);
		box.scrollOffsetX(-50D);
		Assert.assertEquals(Collections.singletonList(-200D), received);
	}

	@Test
	public void firesOnceTheTargetReachesTheEnd() {
		final List<Object> received = new ArrayList<>();
		final ContainerNode box = NodeScrollEventTest.box(1000D).onScrollToEnd(event -> received.addAll(Arrays.asList(event.getAxis(), event.getOffset())));
		this.bridges.open(new NodeUI(box)).frames(30);
		box.scrollOffsetY(-200D);
		box.scrollOffsetY(-900D);
		box.scrollOffsetY(-1000D);
		Assert.assertEquals(Arrays.asList(ScrollAxis.VERTICAL, -700D), received);
	}

	@Test
	public void firesOnceTheScrollReachesTheEnd() {
		final List<Object> received = new ArrayList<>();
		final ContainerNode box = NodeScrollEventTest.box(1000D).onScrollAtEnd(event -> received.add(event.getNode()));
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
		final ContainerNode box = NodeScrollEventTest.box(360D).onScrollAtEnd(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(box)).frames(30);
		this.bridges.move(700D, 200D).frames(2).scroll(-1D).frames(200);
		Assert.assertTrue(received.isEmpty());
		this.bridges.frame().scroll(-1D);
		this.bridges.frame().scroll(-1D).frames(200);
		Assert.assertEquals(Collections.singletonList(box), received);
	}

	@Test
	public void ignoresAScrollBackToTheTop() {
		final List<Object> received = new ArrayList<>();
		final ContainerNode box = NodeScrollEventTest.box(1000D).onScrollAtEnd(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(box)).frames(30);
		box.scrollOffsetY(-200D);
		box.scrollOffsetY(0D);
		this.bridges.frames(200);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void ignoresAnEndLeftBeforeTheScrollRests() {
		final List<Object> received = new ArrayList<>();
		final ContainerNode box = NodeScrollEventTest.box(1000D).onScrollAtEnd(event -> received.add(event.getNode()));
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
		final ContainerNode box = ContainerNode.create(500D, 100D, 300D, 300D).overflow(OverflowProperty.SCROLL).onScrollAtEnd(event -> received.add(event.getNode().getScrollY()));
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