package dev.joid.lib.ui.event.impl.node;

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
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.DraggableProperty;
import dev.joid.lib.ui.node.property.DraggableProperty.DraggableSnapType;

public class NodeDragEventTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void firesOnceTheDragStarted() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free()).onDragStart(event -> received.addAll(Arrays.asList(event.getNode(), event.getNode().isDragging())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList(rect, true), received);
	}

	@Test
	public void waitsForALeftPressOverTheNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free()).onDragStart(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(10D, 10D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.RIGHT);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnEveryMoveOnceTheTargetFollowedTheMouse() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free()).onDrag(event -> received.addAll(Arrays.asList(event.getNode().getTargetDragX(), event.getNode().getTargetDragY())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(400D, 300D).frames(1);
		this.bridges.getUi().mouseMoved();
		this.bridges.move(500D, 350D).frames(1);
		this.bridges.getUi().mouseMoved();
		Assert.assertEquals(Arrays.asList(350D, 250D, 450D, 300D), received);
	}

	@Test
	public void ignoresAMoveWithoutDrag() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free()).onDrag(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mouseMoved();
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnceTheDragEnded() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free()).onDragDrop(event -> received.addAll(Arrays.asList(event.getNode(), event.getNode().isDragging())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertTrue(received.isEmpty());
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList(rect, false), received);
	}

	@Test
	public void keepsTheNodeStillWhenItsDragStartIsRefused() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free()).beforeDragStart(event -> event.cancel()).onDragStart(event -> received.add(event.getNode()));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertFalse(rect.isDragging());
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void givesTheSnapTargetToTheDropListeners() {
		final List<Object> received = new ArrayList<>();
		final RectNode near = RectNode.create(500D, 400D, 200D, 100D);
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free().snap(near)).beforeDragDrop(event -> received.add(event.getTarget())).onDragDrop(event -> received.add(event.getTarget()));
		this.bridges.open(new NodeUI(near, rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(450D, 350D).frames(1);
		this.bridges.getUi().mouseMoved();
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList(near, near), received);
	}

	@Test
	public void firesOnceTheTargetMovedOnTheNearestSnap() {
		final List<Object> received = new ArrayList<>();
		final RectNode near = RectNode.create(500D, 400D, 200D, 100D);
		final RectNode far = RectNode.create(1600D, 900D, 200D, 100D);
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free().snap(near, far)).onDragSnap(event -> received.addAll(Arrays.asList(event.getNode(), event.getTarget(), event.getNode().getTargetDragX(), event.getNode().getTargetDragY())));
		this.bridges.open(new NodeUI(near, far, rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(450D, 350D).frames(1);
		this.bridges.getUi().mouseMoved();
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList(rect, near, 500D, 400D), received);
	}

	@Test
	public void ignoresAReleaseAwayFromAnOverlapTarget() {
		final List<Object> received = new ArrayList<>();
		final RectNode target = RectNode.create(1200D, 700D, 200D, 100D);
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free().snap(DraggableSnapType.OVERLAP, target)).onDragSnap(event -> received.add(event.getTarget()));
		this.bridges.open(new NodeUI(target, rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertTrue(received.isEmpty());
		Assert.assertEquals(100D, rect.getTargetDragX(), 0D);
		Assert.assertEquals(100D, rect.getTargetDragY(), 0D);
	}

	@Test
	public void snapsBeforeItsDrop() {
		final List<Object> received = new ArrayList<>();
		final RectNode target = RectNode.create(1200D, 700D, 200D, 100D);
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free().snap(target)).onDragSnap(event -> received.add("snap")).onDragDrop(event -> received.add("drop"));
		this.bridges.open(new NodeUI(target, rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("snap", "drop"), received);
	}

	@Test
	public void skipsTheSnapOfARefusedDrop() {
		final List<Object> received = new ArrayList<>();
		final RectNode target = RectNode.create(1200D, 700D, 200D, 100D);
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free().snap(target)).beforeDragDrop(event -> event.cancel()).onDragSnap(event -> received.add("snap")).onDragDrop(event -> received.add("drop"));
		this.bridges.open(new NodeUI(target, rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertTrue(received.isEmpty());
		Assert.assertEquals(100D, rect.getTargetDragX(), 0D);
		Assert.assertEquals(100D, rect.getTargetDragY(), 0D);
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