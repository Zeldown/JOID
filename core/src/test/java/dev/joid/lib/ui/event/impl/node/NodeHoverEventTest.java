package dev.joid.lib.ui.event.impl.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.ui.core.NodeUI;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class NodeHoverEventTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void firesOnEveryFrameOverTheNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onHover(event -> received.addAll(Arrays.asList(event.getNode(), event.getMouseX(), event.getMouseY())));
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
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onHover(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2);
		rect.visible(() -> false);
		this.bridges.frames(3);
		Assert.assertEquals(2, received.size());
	}

	@Test
	public void firesOnceWhenTheMouseEntersTheNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onHoverStart(event -> received.addAll(Arrays.asList(event.getNode(), event.getMouseX(), event.getMouseY())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		Assert.assertTrue(received.isEmpty());
		this.bridges.move(150D, 160D).frames(3);
		Assert.assertEquals(Arrays.asList(rect, 150D, 160D), received);
	}

	@Test
	public void firesAgainOnEachNewEntry() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onHoverStart(event -> received.add(event.getMouseX()));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2);
		this.bridges.move(10D, 10D).frames(2);
		this.bridges.move(250D, 160D).frames(2);
		Assert.assertEquals(Arrays.asList(150D, 250D), received);
	}

	@Test
	public void hoversADisabledNodeWithoutItsHoverColor() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).enabled(() -> false).cursor(Cursor.NOT_ALLOWED).onHoverStart(event -> received.add(event.getNode()));
		final NodeUI ui = new NodeUI(rect);
		this.bridges.open(ui).frames(30);
		this.bridges.move(150D, 160D).frames(30);
		Assert.assertEquals(Collections.singletonList(rect), received);
		Assert.assertTrue(rect.isHovered());
		Assert.assertEquals(ui.getHoveredPath().contains(rect), rect.isHovered());
		Assert.assertEquals(0F, rect.hoverValue(1F), 0F);
		Assert.assertSame(Cursor.NOT_ALLOWED, ui.getHoveredNode().getResolvedCursor());
	}

	@Test
	public void firesOnceWhenTheMouseLeavesTheNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onHoverEnd(event -> received.addAll(Arrays.asList(event.getNode(), event.getMouseX(), event.getMouseY())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(3);
		Assert.assertTrue(received.isEmpty());
		this.bridges.move(10D, 20D).frames(3);
		Assert.assertEquals(Arrays.asList(rect, 10D, 20D), received);
	}

	@Test
	public void waitsForTheMouseToHaveEntered() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onHoverEnd(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(10D, 20D).frames(2);
		this.bridges.move(400D, 20D).frames(2);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void keepsTheHoverOfANodeDisabledUnderTheMouse() {
		final List<Object> received = new ArrayList<>();
		final AtomicBoolean enabled = new AtomicBoolean(true);
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).enabled(() -> enabled.get()).onHoverEnd(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(30);
		Assert.assertEquals(1F, rect.hoverValue(1F), 0F);
		enabled.set(false);
		this.bridges.frames(30);
		Assert.assertTrue(received.isEmpty());
		Assert.assertTrue(rect.isHovered());
		Assert.assertEquals(0F, rect.hoverValue(1F), 0F);
	}

}