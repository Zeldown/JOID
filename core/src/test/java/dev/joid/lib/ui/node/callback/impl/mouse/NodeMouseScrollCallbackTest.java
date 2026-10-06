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
import dev.joid.lib.utils.context.InternalContext;

public class NodeMouseScrollCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeMouseScrollCallback<RectNode> callback = (node, mouseX, mouseY, value) -> received.addAll(Arrays.asList(node, mouseX, mouseY, value));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final InternalContext context = InternalContext.create();
		callback.pre(rect, context, 3D, 4D, -120);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context, 3D, 4D, -120);
		Assert.assertEquals(Arrays.asList(rect, 3D, 4D, -120), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeMouseScrollCallback<RectNode> callback = (node, mouseX, mouseY, value) -> received.add(node);
		callback.post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create(true), 3D, 4D, -120);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void receivesTheWheelValueAndTheMouse() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onMouseScroll((node, mouseX, mouseY, value) -> received.addAll(Arrays.asList(node, mouseX, mouseY, value)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2).scroll(120);
		this.bridges.move(900D, 700D).frames(2).scroll(-240);
		Assert.assertEquals(Arrays.asList(rect, 150D, 160D, 120, rect, 900D, 700D, -240), received);
	}

	@Test
	public void ignoresAStillWheel() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onMouseScroll((node, mouseX, mouseY, value) -> received.add(node));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2).scroll(0);
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