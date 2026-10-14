package dev.joid.lib.ui.node;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.impl.node.NodeCharEvent;
import dev.joid.lib.ui.event.impl.node.NodeKeyEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseScrollEvent;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class INodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void handlesNoEventByDefault() {
		final INode node = new BareNode();
		final RectNode owner = RectNode.create(0D, 0D, 0D, 0D);
		final NodeKeyEvent<RectNode> input = NodeKeyEvent.create(owner, Key.A, null);
		node.mousePressed(NodeMouseEvent.create(owner, 0D, 0D, MouseButton.LEFT, 0L, input));
		node.mouseDragged(NodeMouseEvent.create(owner, 0D, 0D, MouseButton.LEFT, 16L, input));
		node.mouseReleased(NodeMouseEvent.create(owner, 0D, 0D, MouseButton.LEFT, 0L, input));
		node.mouseScroll(NodeMouseScrollEvent.create(owner, 0D, 0D, 0D, 1D, input));
		node.keyPressed(NodeKeyEvent.create(owner, Key.A, input));
		node.charTyped(NodeCharEvent.create(owner, 'a', input));
		Assert.assertFalse(input.isConsumed());
	}

	@Test
	public void drawsNothingByDefault() {
		final INode node = new BareNode();
		node.init(new EmptyUI());
		node.draw(0D, 0D);
		node.drawSkeleton(0D, 0D);
		node.update();
		node.detach();
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	public static final class BareNode implements INode {

		@Override
		public int getIndex() {
			return 0;
		}

	}

	public static final class EmptyUI extends UI {}

}