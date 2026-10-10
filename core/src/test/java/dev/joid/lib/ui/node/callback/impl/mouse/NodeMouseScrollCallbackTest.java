package dev.joid.lib.ui.node.callback.impl.mouse;

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
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import lombok.NonNull;

public class NodeMouseScrollCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeWithoutConsumingTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeMouseScrollCallback<RectNode> callback = (node, mouseX, mouseY, valueX, value) -> received.addAll(Arrays.asList(node, mouseX, mouseY, valueX, value));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final DispatchContext context = DispatchContext.create();
		callback.pre(rect, context, 3D, 4D, 0.5D, -1D);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context, 3D, 4D, 0.5D, -1D);
		Assert.assertEquals(Arrays.asList(rect, 3D, 4D, 0.5D, -1D), received);
		Assert.assertFalse(context.isCancelled());
	}

	@Test
	public void hearsAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeMouseScrollCallback<RectNode> callback = (node, mouseX, mouseY, valueX, value) -> received.add(node);
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		callback.post(rect, DispatchContext.create(true), 3D, 4D, 0D, 1D);
		Assert.assertEquals(Collections.singletonList(rect), received);
	}

	@Test
	public void hearsAWheelConsumedByAnotherNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).onMouseScroll((node, mouseX, mouseY, valueX, value) -> received.add("parent"));
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).onMouseScroll(new NodeMouseScrollCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode node, final double mouseX, final double mouseY, final double notchesX, final double notchesY) {
				received.add("child");
			}

			@Override
			public void post(final @NonNull RectNode node, final @NonNull DispatchContext context, final double mouseX, final double mouseY, final double notchesX, final double notchesY) {
				context.cancel(() -> this.apply(node, mouseX, mouseY, notchesX, notchesY));
			}

		});
		final RectNode sibling = RectNode.create(400D, 100D, 200D, 100D).onMouseScroll((node, mouseX, mouseY, valueX, value) -> received.add("sibling"));
		parent.append(child);
		this.bridges.open(new NodeUI(sibling, parent)).frames(30);
		this.bridges.move(120D, 120D).frames(2);
		Assert.assertTrue(this.bridges.getUi().mouseScroll(0D, 1D));
		Assert.assertEquals(Arrays.asList("child", "parent", "sibling"), received);
	}

	@Test
	public void receivesTheWheelValuesAndTheMouse() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onMouseScroll((node, mouseX, mouseY, valueX, value) -> received.addAll(Arrays.asList(node, mouseX, mouseY, valueX, value)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2).scroll(1D);
		this.bridges.move(900D, 700D).frames(2).scroll(-2D);
		this.bridges.scroll(-1.5D, 0D);
		Assert.assertEquals(Arrays.asList(rect, 150D, 160D, 0D, 1D, rect, 900D, 700D, 0D, -2D, rect, 900D, 700D, -1.5D, 0D), received);
	}

	@Test
	public void ignoresAStillWheel() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onMouseScroll((node, mouseX, mouseY, valueX, value) -> received.add(node));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2).scroll(0D);
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