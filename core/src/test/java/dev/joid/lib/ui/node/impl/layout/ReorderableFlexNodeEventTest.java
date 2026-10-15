package dev.joid.lib.ui.node.impl.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class ReorderableFlexNodeEventTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void firesOnceTheDragStarted() {
		final List<Object> received = new ArrayList<>();
		final RectNode second = RectNode.create(0D, 0D, 200D, 50D);
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).<ReorderableFlexNode>margin(10D).onReorderStart(event -> received.addAll(Arrays.asList(event.getNode(), event.getChild(), event.getNode().isDragging(event.getChild()), event.getOldIndex())));
		flex.append(RectNode.create(0D, 0D, 200D, 50D), second);
		this.bridges.open(new NodeUI(flex)).move(150D, 180D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList(flex, second, true, 1), received);
		this.bridges.move(150D, 300D).frames(10);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.frames(60);
		Assert.assertEquals(4, received.size());
	}

	@Test
	public void firesForADragStartedFromCode() {
		final List<Object> received = new ArrayList<>();
		final RectNode first = RectNode.create(0D, 0D, 200D, 50D);
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).autoDrag(false).onReorderStart(event -> received.add(event.getChild()));
		flex.append(first);
		this.bridges.open(new NodeUI(flex));
		flex.startDrag(first);
		Assert.assertEquals(Collections.singletonList(first), received);
	}

	@Test
	public void waitsForALeftPressOverAChild() {
		final List<Object> received = new ArrayList<>();
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).onReorderStart(event -> received.add(event.getChild()));
		flex.append(RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.RIGHT);
		this.bridges.move(150D, 400D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void runsEveryCallback() {
		final List<Object> received = new ArrayList<>();
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).onReorderStart(event -> received.add("first")).onReorderStart(event -> received.add("second"));
		flex.append(RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("first", "second"), received);
	}

	@Test
	public void startsNothingWhenTheChildRefusesItsDragStart() {
		final List<Object> received = new ArrayList<>();
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).onReorderStart(event -> received.add(event.getChild()));
		final RectNode child = RectNode.create(0D, 0D, 200D, 50D).beforeDragStart(event -> event.cancel());
		flex.append(child);
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(flex.isDragging(child));
		Assert.assertFalse(child.isDragging());
	}

	@Test
	public void firesOnlyWhenTheOrderChanges() {
		final List<Object> received = new ArrayList<>();
		final RectNode first = RectNode.create(0D, 0D, 200D, 50D);
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).<ReorderableFlexNode>margin(10D).onReorder(event -> received.addAll(Arrays.asList(event.getNode(), event.getChild())));
		flex.append(first, RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.frames(2);
		Assert.assertTrue(received.isEmpty());
		this.bridges.move(150D, 300D).frames(60);
		Assert.assertEquals(Arrays.asList(flex, first), received);
	}

	@Test
	public void followsTheIndexOfTheDraggedChild() {
		final List<Object> indexes = new ArrayList<>();
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).<ReorderableFlexNode>margin(10D).onReorder(event -> indexes.add(event.getNewIndex()));
		flex.append(RectNode.create(0D, 0D, 200D, 50D), RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.frame().move(150D, 300D).frames(60);
		Assert.assertEquals(Arrays.asList(1), indexes);
	}

	@Test
	public void staysQuietOnceReleased() {
		final List<Object> received = new ArrayList<>();
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).<ReorderableFlexNode>margin(10D).onReorder(event -> received.add(event.getChild()));
		flex.append(RectNode.create(0D, 0D, 200D, 50D), RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(150D, 300D).frames(5);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		final int count = received.size();
		this.bridges.frames(60);
		Assert.assertEquals(count, received.size());
	}

	@Test
	public void keepsTheOrderWhenAReorderIsRefused() {
		final List<Object> received = new ArrayList<>();
		final List<Object> proposals = new ArrayList<>();
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).beforeReorder(event -> {
			proposals.add(event.getOldIndex() + " -> " + event.getNewIndex());
			event.cancel();
		}).onReorder(event -> received.add(event.getChild()));
		final RectNode first = RectNode.create(0D, 0D, 200D, 50D);
		final RectNode second = RectNode.create(0D, 0D, 200D, 50D);
		flex.append(first, second);
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(150D, 300D).frames(60);
		Assert.assertTrue(received.isEmpty());
		Assert.assertEquals("0 -> 1", proposals.get(0));
		Assert.assertEquals(Arrays.asList(first, second), flex.getLogicalOrder());
	}

	@Test
	public void firesOnceTheChildSettledOnItsSlot() {
		final List<Object> received = new ArrayList<>();
		final RectNode first = RectNode.create(0D, 0D, 200D, 50D);
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).<ReorderableFlexNode>margin(10D).onReorderEnd(event -> received.addAll(Arrays.asList(event.getNode(), event.getChild(), event.getOldIndex(), event.getNewIndex(), event.getNode().getChildIndex(event.getChild()), event.getChild().getY(), event.getNode().isDragging(event.getChild()))));
		flex.append(first, RectNode.create(0D, 0D, 200D, 50D), RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(150D, 1000D).frames(60);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertTrue(received.isEmpty());
		this.bridges.frames(60);
		Assert.assertEquals(Arrays.asList(flex, first, 0, 2, 2, 120D, false), received);
	}

	@Test
	public void reportsTheSameIndexForADropInPlace() {
		final List<Object> received = new ArrayList<>();
		final RectNode second = RectNode.create(0D, 0D, 200D, 50D);
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).<ReorderableFlexNode>margin(10D).onReorderEnd(event -> received.addAll(Arrays.asList(event.getChild(), event.getOldIndex(), event.getNewIndex())));
		flex.append(RectNode.create(0D, 0D, 200D, 50D), second);
		this.bridges.open(new NodeUI(flex)).move(150D, 180D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.frames(10);
		Assert.assertEquals(Arrays.asList(second, 1, 1), received);
	}

	@Test
	public void givesRealDragEventsToTheDraggedChild() {
		final List<Object> received = new ArrayList<>();
		final RectNode first = RectNode.create(0D, 0D, 200D, 50D);
		first.onDragStart(event -> received.add("start " + event.getNode().isDragging())).onDrag(event -> {
			if (!received.contains("drag")) {
				received.add("drag");
			}
		}).onDragDrop(event -> received.add("drop " + event.getNode().isDragging()));
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).margin(10D);
		flex.append(first, RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(150D, 300D).frames(10);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.frames(60);
		Assert.assertEquals(Arrays.asList("start true", "drag", "drop true"), received);
		Assert.assertFalse(first.isDragging());
	}

	@Test
	public void sendsTheChildBackWhenItsDropIsRefused() {
		final List<Object> received = new ArrayList<>();
		final RectNode first = RectNode.create(0D, 0D, 200D, 50D).beforeDragDrop(event -> event.cancel());
		final RectNode second = RectNode.create(0D, 0D, 200D, 50D);
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).<ReorderableFlexNode>margin(10D).onReorderEnd(event -> received.addAll(Arrays.asList(event.getOldIndex(), event.getNewIndex())));
		flex.append(first, second);
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(150D, 300D).frames(60);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.frames(60);
		Assert.assertEquals(Arrays.asList(0, 0), received);
		Assert.assertEquals(Arrays.asList(first, second), flex.getChildren());
	}

	@Test
	public void dropsTheDraggedChildWhenDetachedDuringTheDrag() {
		final List<Object> received = new ArrayList<>();
		final RectNode first = RectNode.create(0D, 0D, 200D, 50D).onDragDrop(event -> received.add("drop"));
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).onReorderEnd(event -> received.add("end"));
		flex.append(first, RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		flex.fireDetach();
		Assert.assertEquals(Arrays.asList("drop", "end"), received);
	}

	@Test
	public void endsTheReorderWhenTheDraggedChildIsRemoved() {
		final List<Object> received = new ArrayList<>();
		final RectNode first = RectNode.create(0D, 0D, 200D, 50D).onDragDrop(event -> received.add("drop"));
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(100D, 100D, 200D).onReorderEnd(event -> received.add("end"));
		flex.append(first, RectNode.create(0D, 0D, 200D, 50D));
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		flex.remove(first);
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("drop", "end"), received);
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