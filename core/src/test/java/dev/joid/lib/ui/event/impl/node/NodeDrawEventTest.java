package dev.joid.lib.ui.event.impl.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class NodeDrawEventTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void firesOnEveryFrameWithTheMouse() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onDraw(event -> received.addAll(Arrays.asList(event.getNode(), event.getMouseX(), event.getMouseY())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		received.clear();
		this.bridges.move(900D, 700D).frames(2);
		Assert.assertEquals(Arrays.asList(rect, 900D, 700D, rect, 900D, 700D), received);
	}

	@Test
	public void firesOnceTheNodeDrewItself() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).onDraw(event -> received.add(this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).size()));
		this.bridges.open(new NodeUI(rect)).frames(30);
		Assert.assertFalse(received.isEmpty());
		Assert.assertFalse(received.contains(0));
	}

	@Test
	public void firesOnEveryFrameWithTheMouseRender() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onRender(event -> received.addAll(Arrays.asList(event.getNode(), event.getMouseX(), event.getMouseY())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		received.clear();
		this.bridges.move(150D, 160D).frames(2);
		Assert.assertEquals(Arrays.asList(rect, 150D, 160D, rect, 150D, 160D), received);
	}

	@Test
	public void firesOnceTheChildrenAreDrawn() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).onRender(event -> received.add(this.bridges.getRender().getDraws(0.6F, 0.4F, 0.2F).size()));
		RectNode.create(10D, 10D, 20D, 20D).color(new Color(0.6F, 0.4F, 0.2F, 1F)).attach(parent);
		this.bridges.open(new NodeUI(parent)).frames(30);
		Assert.assertFalse(received.isEmpty());
		Assert.assertFalse(received.contains(0));
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