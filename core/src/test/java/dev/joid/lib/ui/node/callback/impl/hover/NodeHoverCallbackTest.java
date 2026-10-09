package dev.joid.lib.ui.node.callback.impl.hover;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class NodeHoverCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeHoverCallback<RectNode> callback = (node, mouseX, mouseY) -> received.addAll(Arrays.asList(node, mouseX, mouseY));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final DispatchContext context = DispatchContext.create();
		callback.pre(rect, context, 3D, 4D);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context, 3D, 4D);
		Assert.assertEquals(Arrays.asList(rect, 3D, 4D), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeHoverCallback<RectNode> callback = (node, mouseX, mouseY) -> received.add(node);
		callback.post(RectNode.create(0D, 0D, 10D, 10D), DispatchContext.create(true), 3D, 4D);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnEveryFrameOverTheNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onHover((node, mouseX, mouseY) -> received.addAll(Arrays.asList(node, mouseX, mouseY)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		Assert.assertTrue(received.isEmpty());
		this.bridges.move(150D, 160D).frames(1);
		this.bridges.move(170D, 180D).frames(1);
		this.bridges.move(10D, 20D).frames(3);
		Assert.assertEquals(Arrays.asList(rect, 150D, 160D, rect, 170D, 180D), received);
	}

	@Test
	public void stopsOnceTheNodeIsHidden() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onHover((node, mouseX, mouseY) -> received.add(node));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2);
		rect.visible(node -> false);
		this.bridges.frames(3);
		Assert.assertEquals(2, received.size());
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