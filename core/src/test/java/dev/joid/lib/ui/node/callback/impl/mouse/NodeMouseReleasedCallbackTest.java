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

public class NodeMouseReleasedCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeWithoutConsumingTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeMouseReleasedCallback<RectNode> callback = (node, mouseX, mouseY, button) -> received.addAll(Arrays.asList(node, mouseX, mouseY, button));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final DispatchContext context = DispatchContext.create();
		callback.pre(rect, context, 3D, 4D, MouseButton.RIGHT);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context, 3D, 4D, MouseButton.RIGHT);
		Assert.assertEquals(Arrays.asList(rect, 3D, 4D, MouseButton.RIGHT), received);
		Assert.assertFalse(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeMouseReleasedCallback<RectNode> callback = (node, mouseX, mouseY, button) -> received.add(node);
		callback.post(RectNode.create(0D, 0D, 10D, 10D), DispatchContext.create(true), 3D, 4D, MouseButton.RIGHT);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void receivesTheReleasedButtonAndTheMouse() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onMouseReleased((node, mouseX, mouseY, button) -> received.addAll(Arrays.asList(node, mouseX, mouseY, button)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertTrue(received.isEmpty());
		this.bridges.move(700D, 500D).frames(2);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList(rect, 700D, 500D, MouseButton.LEFT), received);
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