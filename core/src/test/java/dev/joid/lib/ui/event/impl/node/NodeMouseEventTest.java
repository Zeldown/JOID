package dev.joid.lib.ui.event.impl.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.core.NodeUI;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class NodeMouseEventTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void skipsTheNextListenersOnceAPressIsConsumed() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).handleMousePressed(event -> received.add("parent"));
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).onClick(event -> received.add("child"));
		final RectNode sibling = RectNode.create(400D, 100D, 200D, 100D).handleMousePressed(event -> received.add("sibling")).handleMousePressed(EventListener.priority(EventPriority.MONITOR, event -> received.add("sibling monitor")));
		parent.append(child);
		this.bridges.open(new NodeUI(sibling, parent)).frames(30);
		this.bridges.move(120D, 120D).frames(2);
		Assert.assertTrue(this.bridges.getUi().mousePressed(MouseButton.LEFT));
		Assert.assertEquals(Arrays.asList("child", "sibling monitor"), received);
	}

	@Test
	public void receivesAPressAnywhereInTheUi() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).handleMousePressed(event -> received.addAll(Arrays.asList(event.getNode(), event.getMouseX(), event.getMouseY(), event.getButton())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(10D, 20D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.RIGHT);
		Assert.assertEquals(Arrays.asList(rect, 10D, 20D, MouseButton.RIGHT), received);
	}

	@Test
	public void ignoresAPressOnAHiddenOrDisabledNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode hidden = RectNode.create(100D, 100D, 200D, 100D).visible(() -> false).handleMousePressed(event -> received.add(event.getNode()));
		final RectNode child = RectNode.create(0D, 0D, 20D, 20D).handleMousePressed(event -> received.add(event.getNode()));
		hidden.append(child);
		final RectNode disabled = RectNode.create(400D, 100D, 200D, 100D).enabled(() -> false).handleMousePressed(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(hidden, disabled)).frames(30);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void letsEveryListenerAndTheClickBehindReceiveThePress() {
		final List<Object> received = new ArrayList<>();
		final RectNode back = RectNode.create(100D, 100D, 200D, 100D).onClick(event -> received.add("click"));
		final RectNode first = RectNode.create(500D, 100D, 20D, 20D).handleMousePressed(event -> received.add("first"));
		final RectNode second = RectNode.create(600D, 100D, 20D, 20D).handleMousePressed(event -> received.add("second"));
		this.bridges.open(new NodeUI(back, first, second)).frames(30);
		this.bridges.move(150D, 160D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("second", "first", "click"), received);
	}

	@Test
	public void receivesAClickOnlyOverTheNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onClick(event -> received.addAll(Arrays.asList(event.getNode(), event.getMouseX(), event.getMouseY(), event.getButton())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(10D, 20D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertTrue(received.isEmpty());
		this.bridges.move(150D, 160D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.MIDDLE);
		Assert.assertTrue(received.isEmpty());
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList(rect, 150D, 160D, MouseButton.LEFT), received);
	}

	@Test
	public void givesTheClickToTheFrontNodeOnly() {
		final List<Object> received = new ArrayList<>();
		final RectNode back = RectNode.create(100D, 100D, 200D, 100D).onClick(event -> received.add(event.getNode()));
		final RectNode front = RectNode.create(120D, 120D, 100D, 50D).onClick(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(back, front)).frames(30);
		this.bridges.move(150D, 140D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Collections.singletonList(front), received);
		this.bridges.move(280D, 180D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList(front, back), received);
	}

	@Test
	public void runsEveryClickCallbackOfANode() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onClick(event -> received.add("first")).onClick(event -> received.add("second"));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("first", "second"), received);
	}

	@Test
	public void runsThePressAndTheClickOfANode() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onClick(event -> received.add("click")).handleMousePressed(event -> received.add("press"));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("press", "click"), received);
	}

	@Test
	public void skipsTheNextListenersOnceAReleaseIsConsumed() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).handleMouseReleased(event -> received.add("parent"));
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).handleMouseReleased(event -> {
			received.add("child");
			event.consume();
		});
		final RectNode sibling = RectNode.create(400D, 100D, 200D, 100D).handleMouseReleased(event -> received.add("sibling")).handleMouseReleased(EventListener.priority(EventPriority.MONITOR, event -> received.add("sibling monitor")));
		parent.append(child);
		this.bridges.open(new NodeUI(sibling, parent)).frames(30);
		this.bridges.move(120D, 120D).frames(2);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("child", "sibling monitor"), received);
	}

	@Test
	public void receivesTheReleasedButtonAndTheMouse() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).handleMouseReleased(event -> received.addAll(Arrays.asList(event.getNode(), event.getMouseX(), event.getMouseY(), event.getButton())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertTrue(received.isEmpty());
		this.bridges.move(700D, 500D).frames(2);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList(rect, 700D, 500D, MouseButton.LEFT), received);
	}

	@Test
	public void skipsTheNextListenersOnceAMoveIsConsumed() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).handleMouseDragged(event -> received.add("parent"));
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).handleMouseDragged(event -> {
			received.add("child");
			event.consume();
		});
		final RectNode sibling = RectNode.create(400D, 100D, 200D, 100D).handleMouseDragged(event -> received.add("sibling")).handleMouseDragged(EventListener.priority(EventPriority.MONITOR, event -> received.add("sibling monitor")));
		parent.append(child);
		this.bridges.open(new NodeUI(sibling, parent)).frames(30);
		this.bridges.move(120D, 120D).getUi().mousePressed(MouseButton.LEFT);
		Assert.assertTrue(this.bridges.frames(1).getUi().mouseMoved());
		Assert.assertEquals(Arrays.asList("child", "sibling monitor"), received);
	}

	@Test
	public void receivesEachMoveWithItsButtonAndDelay() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).handleMouseDragged(event -> received.addAll(Arrays.asList(event.getNode(), event.getMouseX(), event.getMouseY(), event.getButton(), event.getDeltaTime())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).getUi().mousePressed(MouseButton.LEFT);
		this.bridges.frames(1).getUi().mouseMoved();
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.move(400D, 300D).getUi().mousePressed(MouseButton.RIGHT);
		this.bridges.frames(2).getUi().mouseMoved();
		Assert.assertEquals(Arrays.asList(rect, 150D, 160D, MouseButton.LEFT, 16L, rect, 400D, 300D, MouseButton.RIGHT, 32L), received);
	}

}