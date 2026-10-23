package dev.joid.lib.ui.event.impl.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.NodeUI;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class NodeMouseScrollEventTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void skipsTheNextListenersOnceAWheelIsConsumed() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).handleMouseScroll(event -> received.add("parent"));
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).handleMouseScroll(event -> {
			received.add("child");
			event.consume();
		});
		final RectNode sibling = RectNode.create(400D, 100D, 200D, 100D).handleMouseScroll(event -> received.add("sibling")).handleMouseScroll(EventListener.priority(EventPriority.MONITOR, event -> received.add("sibling monitor")));
		parent.append(child);
		this.bridges.open(new NodeUI(sibling, parent)).frames(30);
		this.bridges.move(120D, 120D).frames(2);
		Assert.assertTrue(this.bridges.getUi().mouseScroll(0D, 1D));
		Assert.assertEquals(Arrays.asList("child", "sibling monitor"), received);
	}

	@Test
	public void receivesTheWheelValuesAndTheMouse() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).handleMouseScroll(event -> received.addAll(Arrays.asList(event.getNode(), event.getMouseX(), event.getMouseY(), event.getNotchesX(), event.getNotchesY())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2).scroll(1D);
		this.bridges.move(900D, 700D).frames(2).scroll(-2D);
		this.bridges.scroll(-1.5D, 0D);
		Assert.assertEquals(Arrays.asList(rect, 150D, 160D, 0D, 1D, rect, 900D, 700D, 0D, -2D, rect, 900D, 700D, -1.5D, 0D), received);
	}

	@Test
	public void ignoresAStillWheel() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).handleMouseScroll(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2).scroll(0D);
		Assert.assertTrue(received.isEmpty());
	}

}