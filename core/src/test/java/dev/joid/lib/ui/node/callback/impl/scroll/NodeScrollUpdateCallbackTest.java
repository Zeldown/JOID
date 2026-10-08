package dev.joid.lib.ui.node.callback.impl.scroll;

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
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

public class NodeScrollUpdateCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeScrollUpdateCallback<RectNode> callback = (node, value) -> received.addAll(Arrays.asList(node, value));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final InternalContext context = InternalContext.create();
		callback.pre(rect, context, -30D);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context, -30D);
		Assert.assertEquals(Arrays.asList(rect, -30D), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeScrollUpdateCallback<RectNode> callback = (node, value) -> received.add(node);
		callback.post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create(true), -30D);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void receivesTheRequestedOffsetOnceTheTargetMoved() {
		final List<Object> received = new ArrayList<>();
		final ContainerNode box = NodeScrollUpdateCallbackTest.box(1000D).onScrollUpdate((node, value) -> received.addAll(Arrays.asList(node, value, node.getTargetScrollY())));
		this.bridges.open(new NodeUI(box)).frames(30);
		box.scrollOffsetY(-200D);
		box.scrollRatioY(0.5F);
		Assert.assertEquals(Arrays.asList(box, -200D, -200D, box, -350D, -350D), received);
	}

	@Test
	public void firesOnEachWheelTick() {
		final List<Object> received = new ArrayList<>();
		final ContainerNode box = NodeScrollUpdateCallbackTest.box(1000D).onScrollUpdate((node, value) -> received.add(value));
		this.bridges.open(new NodeUI(box)).frames(30);
		this.bridges.move(700D, 200D).frames(2).scroll(-1D);
		this.bridges.frame().scroll(-1D);
		Assert.assertEquals(Arrays.asList(-30D, -60D), received);
	}

	@Test
	public void keepsTheScrollInPlaceWhenThePrePhaseConsumesIt() {
		final List<Object> received = new ArrayList<>();
		final ContainerNode box = NodeScrollUpdateCallbackTest.box(1000D).onScrollUpdate(new NodeScrollUpdateCallback<ContainerNode>() {

			@Override
			public void apply(final @NonNull ContainerNode node, final double value) {
				received.add(node);
			}

			@Override
			@NodeCallbackMethod(Type.PRE)
			public void pre(final @NonNull ContainerNode node, final @NonNull InternalContext context, final double value) {
				context.cancel();
			}

		});
		this.bridges.open(new NodeUI(box)).frames(30);
		box.scrollOffsetY(-200D);
		Assert.assertEquals(0D, box.getTargetScrollY(), 0D);
		Assert.assertTrue(received.isEmpty());
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