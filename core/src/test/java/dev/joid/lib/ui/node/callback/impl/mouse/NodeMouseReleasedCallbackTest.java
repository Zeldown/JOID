package dev.joid.lib.ui.node.callback.impl.mouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;

public class NodeMouseReleasedCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeMouseReleasedCallback<RectNode> callback = (node, mouseX, mouseY, clickType) -> received.addAll(Arrays.asList(node, mouseX, mouseY, clickType));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final InternalContext context = InternalContext.create();
		callback.pre(rect, context, 3D, 4D, ClickType.RIGHT);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context, 3D, 4D, ClickType.RIGHT);
		Assert.assertEquals(Arrays.asList(rect, 3D, 4D, ClickType.RIGHT), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeMouseReleasedCallback<RectNode> callback = (node, mouseX, mouseY, clickType) -> received.add(node);
		callback.post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create(true), 3D, 4D, ClickType.RIGHT);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void receivesTheReleasedButtonAndTheMouse() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onMouseReleased((node, mouseX, mouseY, clickType) -> received.addAll(Arrays.asList(node, mouseX, mouseY, clickType)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertTrue(received.isEmpty());
		this.bridges.move(700D, 500D).frames(2);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		Assert.assertEquals(Arrays.asList(rect, 700D, 500D, ClickType.LEFT), received);
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