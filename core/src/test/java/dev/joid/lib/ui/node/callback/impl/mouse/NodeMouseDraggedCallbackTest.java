package dev.joid.lib.ui.node.callback.impl.mouse;

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
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class NodeMouseDraggedCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeWithoutConsumingTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeMouseDraggedCallback<RectNode> callback = (node, mouseX, mouseY, button, deltaTime) -> received.addAll(Arrays.asList(node, mouseX, mouseY, button, deltaTime));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final DispatchContext context = DispatchContext.create();
		callback.pre(rect, context, 3D, 4D, MouseButton.LEFT, 16L);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context, 3D, 4D, MouseButton.LEFT, 16L);
		Assert.assertEquals(Arrays.asList(rect, 3D, 4D, MouseButton.LEFT, 16L), received);
		Assert.assertFalse(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeMouseDraggedCallback<RectNode> callback = (node, mouseX, mouseY, button, deltaTime) -> received.add(node);
		callback.post(RectNode.create(0D, 0D, 10D, 10D), DispatchContext.create(true), 3D, 4D, MouseButton.LEFT, 16L);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void receivesEachMoveWithItsButtonAndDelay() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onMouseDragged((node, mouseX, mouseY, button, deltaTime) -> received.addAll(Arrays.asList(node, mouseX, mouseY, button, deltaTime)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).getUi().mousePressed(MouseButton.LEFT);
		this.bridges.frames(1).getUi().mouseMoved();
		this.bridges.move(400D, 300D).getUi().mousePressed(MouseButton.RIGHT);
		this.bridges.frames(2).getUi().mouseMoved();
		Assert.assertEquals(Arrays.asList(rect, 150D, 160D, MouseButton.LEFT, 16L, rect, 400D, 300D, MouseButton.RIGHT, 32L), received);
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