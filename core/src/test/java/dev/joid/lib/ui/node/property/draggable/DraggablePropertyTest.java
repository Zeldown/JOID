package dev.joid.lib.ui.node.property.draggable;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableAreaType;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableSnapType;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableType;

import lombok.AllArgsConstructor;

public class DraggablePropertyTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void movesAFreeNodeAnywhere() {
		final DraggableProperty draggable = DraggableProperty.free();
		Assert.assertSame(DraggableType.MOVE, draggable.getType());
		Assert.assertSame(DraggableAreaType.FREE, draggable.getAreaType());
		Assert.assertSame(DraggableSnapType.NEAREST, draggable.getSnapType());
		Assert.assertNull(draggable.getAreaObject());
		Assert.assertFalse(draggable.hasSnapping());
		Assert.assertTrue(draggable.isEnabled(RectNode.create(0D, 0D, 10D, 10D)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void hasNoBoundsWhenFree() {
		DraggableProperty.free().getBounds(RectNode.create(0D, 0D, 10D, 10D));
	}

	@Test
	public void staysInsideACustomArea() {
		Assert.assertArrayEquals(new double[] {10D, 20D, 300D, 200D}, DraggableProperty.custom(10D, 20D, 300D, 200D).getBounds(RectNode.create(0D, 0D, 10D, 10D)), 0D);
	}

	@Test
	public void staysInsideItsParent() {
		final RectNode parent = RectNode.create(100D, 50D, 300D, 200D);
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D);
		parent.append(child);
		Assert.assertArrayEquals(new double[] {100D, 50D, 300D, 200D}, DraggableProperty.parent().getBounds(child), 0D);
	}

	@Test
	public void explainsThatANodeWithoutParentHasNoParentArea() {
		try {
			DraggableProperty.parent().getBounds(RectNode.create(10D, 10D, 20D, 20D));
			Assert.fail("A node without parent has no parent area");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The node RectNode is dragged inside its parent but sits at the top of its UI, attach it to a node or pick another area such as DraggableProperty.ui()", expected.getMessage());
		}
	}

	@Test
	public void staysInsideAnotherNode() {
		final RectNode parent = RectNode.create(100D, 50D, 300D, 200D);
		final RectNode area = RectNode.create(10D, 20D, 80D, 60D);
		parent.append(area);
		final DraggableProperty draggable = DraggableProperty.node(area);
		Assert.assertSame(area, draggable.getAreaObject());
		Assert.assertArrayEquals(new double[] {110D, 70D, 80D, 60D}, draggable.getBounds(RectNode.create(0D, 0D, 10D, 10D)), 0D);
	}

	@Test
	public void staysInsideTheDesignedUi() {
		Assert.assertArrayEquals(new double[] {0D, 0D, 1920D, 1080D}, DraggableProperty.ui().getBounds(RectNode.create(0D, 0D, 10D, 10D)), 0D);
	}

	@Test
	public void staysOnTheWholeScreen() {
		this.bridges.resize(2560, 1080);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D);
		this.bridges.open(new NodeUI(node)).frame();
		Assert.assertArrayEquals(new double[] {-320D, 0D, 2560D, 1080D}, DraggableProperty.screen().getBounds(node), 1E-9D);
	}

	@Test
	public void refusesToMoveWhenDisabled() {
		Assert.assertFalse(DraggableProperty.disabled().isEnabled(RectNode.create(0D, 0D, 10D, 10D)));
	}

	@Test
	public void followsItsEnabledPredicate() {
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D);
		final DraggableProperty draggable = DraggableProperty.free();
		Assert.assertSame(draggable, draggable.enabled(target -> target.getX() > 5D));
		Assert.assertFalse(draggable.isEnabled(node));
		Assert.assertTrue(draggable.isEnabled(node.x(10D)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesANodeAreaWithoutNode() {
		DraggableProperty.free().area(DraggableAreaType.NODE, "area");
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesACustomAreaWithoutBounds() {
		DraggableProperty.free().area(DraggableAreaType.CUSTOM, new int[] {0, 0, 10, 10});
	}

	@Test
	public void snapsToTheNearestNode() {
		final RectNode left = RectNode.create(0D, 0D, 10D, 10D);
		final RectNode right = RectNode.create(100D, 0D, 10D, 10D);
		final DraggableProperty draggable = DraggableProperty.free().snap(left).snap(right);
		Assert.assertTrue(draggable.hasSnapping());
		Assert.assertEquals(Arrays.asList(left, right), draggable.getSnapNodes());
		Assert.assertSame(right, draggable.getSnapping(RectNode.create(80D, 0D, 10D, 10D)));
		Assert.assertSame(left, draggable.getSnapping(RectNode.create(20D, 30D, 10D, 10D)));
	}

	@Test
	public void snapsToTheFirstOverlappedNode() {
		final RectNode left = RectNode.create(0D, 0D, 10D, 10D);
		final RectNode right = RectNode.create(100D, 0D, 10D, 10D);
		final DraggableProperty draggable = DraggableProperty.free().snap(DraggableSnapType.OVERLAP, left, right);
		Assert.assertSame(DraggableSnapType.OVERLAP, draggable.getSnapType());
		Assert.assertSame(right, draggable.getSnapping(RectNode.create(95D, 5D, 10D, 10D)));
		Assert.assertNull(draggable.getSnapping(RectNode.create(50D, 5D, 10D, 10D)));
		Assert.assertNull(draggable.getSnapping(RectNode.create(95D, 10D, 10D, 10D)));
	}

	@Test
	public void forgetsItsSnapsWithoutNodes() {
		final DraggableProperty draggable = DraggableProperty.free().snap(RectNode.create(0D, 0D, 10D, 10D)).snap(DraggableSnapType.NEAREST);
		Assert.assertFalse(draggable.hasSnapping());
		Assert.assertNull(draggable.getSnapping(RectNode.create(0D, 0D, 10D, 10D)));
		Assert.assertFalse(DraggableProperty.free().snap(DraggableSnapType.OVERLAP, (Node[]) null).hasSnapping());
		Assert.assertFalse(DraggableProperty.free().snap().hasSnapping());
	}

	@Test
	public void easesTowardsItsTarget() {
		final DraggableProperty draggable = DraggableProperty.free();
		Assert.assertEquals(5D, draggable.lerp(1000D / 60D, 0D, 30D), 1E-9D);
		Assert.assertEquals(25D, draggable.lerp(1000D / 60D, 30D, 0D), 1E-9D);
		Assert.assertEquals(10D, draggable.lerp(1000D / 30D, 0D, 30D), 1E-9D);
		Assert.assertEquals(0.4D, draggable.lerp(1000D / 60D, 0D, 0.4D), 0D);
		Assert.assertEquals(31D, draggable.lerp(1000D, 0D, 31D), 0D);
	}

	@Test
	public void copiesItsAreaAndSnaps() {
		final RectNode target = RectNode.create(0D, 0D, 10D, 10D);
		final DraggableProperty draggable = DraggableProperty.custom(10D, 20D, 300D, 200D).enabled(node -> false).snap(DraggableSnapType.OVERLAP, target);
		final DraggableProperty copy = draggable.copy();
		Assert.assertNotSame(draggable, copy);
		Assert.assertArrayEquals(new double[] {10D, 20D, 300D, 200D}, copy.getBounds(target), 0D);
		Assert.assertFalse(copy.isEnabled(target));
		Assert.assertSame(DraggableSnapType.OVERLAP, copy.getSnapType());
		Assert.assertEquals(Arrays.asList(target), copy.getSnapNodes());
		copy.snap(RectNode.create(0D, 0D, 10D, 10D));
		Assert.assertEquals(1, draggable.getSnapNodes().size());
		Assert.assertFalse(DraggableProperty.free().copy().hasSnapping());
	}

	@Test
	public void dragsANodeWithTheMouse() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free());
		this.drag(node, 210D, 160D);
		this.bridges.frames(100);
		Assert.assertEquals(200D, node.getX(), 0D);
		Assert.assertEquals(150D, node.getY(), 0D);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertFalse(node.isDragging());
	}

	@Test
	public void bringsADroppedNodeBackInsideItsArea() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.custom(0D, 0D, 300D, 300D));
		this.drag(node, 410D, 160D);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.frames(100);
		Assert.assertEquals(250D, node.getX(), 0D);
		Assert.assertEquals(150D, node.getY(), 0D);
	}

	@Test
	public void dropsANodeOnItsSnap() {
		final RectNode target = RectNode.create(400D, 400D, 50D, 50D);
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free().snap(target));
		this.drag(node, 310D, 310D);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.frames(100);
		Assert.assertEquals(400D, node.getX(), 0D);
		Assert.assertEquals(400D, node.getY(), 0D);
	}

	@Test
	public void sendsANodeBackWithoutAnOverlappedSnap() {
		final RectNode target = RectNode.create(400D, 400D, 50D, 50D);
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free().snap(DraggableSnapType.OVERLAP, target));
		this.drag(node, 310D, 310D);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.frames(100);
		Assert.assertEquals(100D, node.getX(), 0D);
		Assert.assertEquals(100D, node.getY(), 0D);
	}

	@Test
	public void dragsACopyOfTheNode() {
		final DraggableProperty draggable = DraggableProperty.free();
		Assert.assertSame(draggable, draggable.type(DraggableType.COPY));
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).draggable(draggable);
		this.drag(ContainerNode.create(0D, 0D, 1920D, 1080D).append(node), 210D, 160D);
		this.bridges.frames(100);
		final List<Draw> draws = this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F);
		Assert.assertNotNull(node.getDraggedNode());
		Assert.assertEquals(200D, node.getDraggedNode().getX(), 0D);
		Assert.assertEquals(100D, node.getX(), 0D);
		Assert.assertEquals(2, draws.size());
		Assert.assertEquals(100D, draws.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(200D, draws.get(1).getLeft(), 1E-3D);
		Assert.assertEquals(150D, draws.get(1).getTop(), 1E-3D);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertNull(node.getDraggedNode());
	}

	@Test
	public void leavesADisabledNodeInPlace() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.disabled());
		this.drag(node, 210D, 160D);
		this.bridges.frames(10);
		Assert.assertFalse(node.isDragging());
		Assert.assertEquals(100D, node.getX(), 0D);
	}

	@Test
	public void copiesItsType() {
		Assert.assertSame(DraggableType.COPY, DraggableProperty.free().type(DraggableType.COPY).copy().getType());
	}

	@Test
	public void drawsTheCopyOfADraggedTopLevelNode() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).draggable(DraggableProperty.free().type(DraggableType.COPY));
		this.bridges.open(new NodeUI(node)).frame();
		this.bridges.move(110D, 110D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(210D, 160D).frame();
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(100);
		Assert.assertEquals(2, this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).size());
	}

	private void drag(final Node node, final double x, final double y) {
		this.bridges.open(new NodeUI(node)).frame();
		this.bridges.move(110D, 110D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(x, y).frame();
		this.bridges.getUi().mouseMoved();
	}

	@AllArgsConstructor
	public static final class NodeUI extends UI {

		private final Node node;

		@Override
		public void init() {
			super.add(this.node);
		}

	}

}