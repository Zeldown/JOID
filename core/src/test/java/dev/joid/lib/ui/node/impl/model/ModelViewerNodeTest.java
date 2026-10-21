package dev.joid.lib.ui.node.impl.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.draw.model.IDrawableModel;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.geom.Point;
import lombok.AllArgsConstructor;

public class ModelViewerNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void startsAtItsNaturalZoomWithAZoomRange() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D);
		Assert.assertEquals(1D, viewer.getZoom(), 0D);
		Assert.assertEquals(1D, viewer.getDrawnZoom(), 0D);
		Assert.assertEquals(0.1D, viewer.getMinZoom(), 0D);
		Assert.assertEquals(2D, viewer.getMaxZoom(), 0D);
		Assert.assertEquals(-Double.MAX_VALUE, viewer.getMinRotationYaw(), 0D);
		Assert.assertEquals(Double.MAX_VALUE, viewer.getMaxRotationPitch(), 0D);
		Assert.assertFalse(ModelViewerNodeTest.isDragged(viewer));
	}

	@Test
	public void showsItsFirstValuesWithoutEasing() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel());
		Assert.assertSame(viewer, viewer.zoom(1.5D).rotationYaw(30D).rotationPitch(20D));
		this.bridges.open(new NodeUI(viewer)).frame();
		Assert.assertEquals(1.5D, viewer.getDrawnZoom(), 0D);
		Assert.assertEquals(30D, viewer.getDrawnRotationYaw(), 0D);
		Assert.assertEquals(20D, viewer.getDrawnRotationPitch(), 0D);
	}

	@Test
	public void easesTowardsAValueSetOnceOpen() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel());
		this.bridges.open(new NodeUI(viewer)).frame();
		viewer.rotationYaw(30D);
		this.bridges.frame();
		Assert.assertEquals(30D, viewer.getRotationYaw(), 0D);
		Assert.assertTrue(viewer.getDrawnRotationYaw() > 0D && viewer.getDrawnRotationYaw() < 30D);
		this.bridges.frames(600);
		Assert.assertEquals(30D, viewer.getDrawnRotationYaw(), 0D);
	}

	@Test
	public void zoomsInsideItsZoomRange() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D);
		Assert.assertEquals(2D, viewer.zoom(5D).getZoom(), 0D);
		Assert.assertEquals(0.1D, viewer.zoom(0D).getZoom(), 0D);
		Assert.assertSame(viewer, viewer.zoomRange(0.5D, 3D));
		Assert.assertEquals(3D, viewer.zoom(5D).getZoom(), 0D);
		Assert.assertEquals(0.5D, viewer.zoom(0D).getZoom(), 0D);
		Assert.assertEquals(1.2D, viewer.zoom(1.2D).getZoom(), 0D);
	}

	@Test
	public void refusesAZoomRangeWithItsMinAboveItsMax() {
		try {
			ModelViewerNode.create(100D, 100D, 200D, 200D).zoomRange(2D, 0.5D);
			Assert.fail("A min above the max leaves the zoom range empty");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The min 2.0 of the zoom range is above its max 0.5, pass a min of at most 0.5", expected.getMessage());
		}
	}

	@Test
	public void refusesARotationYawRangeWithItsMinAboveItsMax() {
		try {
			ModelViewerNode.create(100D, 100D, 200D, 200D).rotationYawRange(5D, -5D);
			Assert.fail("A min above the max leaves the rotation yaw range empty");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The min 5.0 of the rotation yaw range is above its max -5.0, pass a min of at most -5.0", expected.getMessage());
		}
	}

	@Test
	public void refusesARotationPitchRangeWithItsMinAboveItsMax() {
		try {
			ModelViewerNode.create(100D, 100D, 200D, 200D).rotationPitchRange(2D, -2D);
			Assert.fail("A min above the max leaves the rotation pitch range empty");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The min 2.0 of the rotation pitch range is above its max -2.0, pass a min of at most -2.0", expected.getMessage());
		}
	}

	@Test
	public void bringsItsValuesInsideARangeGivenAfterThem() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).zoom(1.5D).rotationYaw(30D).rotationPitch(-20D);
		viewer.zoomRange(0.1D, 1D).rotationYawRange(-5D, 5D).rotationPitchRange(-2D, 2D);
		Assert.assertEquals(1D, viewer.getZoom(), 0D);
		Assert.assertEquals(5D, viewer.getRotationYaw(), 0D);
		Assert.assertEquals(-2D, viewer.getRotationPitch(), 0D);
	}

	@Test
	public void keepsItsValuesGivenBeforeAWiderRange() {
		final DoubleSignal zoom = new DoubleSignal(3D);
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).zoom(3D).zoomRange(1D, 5D).rotationYaw(30D).rotationYawRange(-5D, 5D).rotationPitch(10D).rotationYawRange(-50D, 50D);
		Assert.assertEquals(3D, viewer.getZoom(), 0D);
		Assert.assertEquals(30D, viewer.getRotationYaw(), 0D);
		Assert.assertEquals(10D, viewer.getRotationPitch(), 0D);
		Assert.assertEquals(3D, ModelViewerNode.create(100D, 100D, 200D, 200D).zoom(zoom).zoomRange(1D, 5D).getZoom(), 0D);
		Assert.assertEquals(3D, zoom.peek(), 0D);
	}

	@Test
	public void zoomsInsideASuppliedZoomRange() {
		final double[] max = {3D};
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel());
		Assert.assertSame(viewer, viewer.zoomRange(() -> 0.5D, () -> max[0]));
		this.bridges.open(new NodeUI(viewer));
		max[0] = 4D;
		this.bridges.frame();
		Assert.assertEquals(4D, viewer.zoom(5D).getZoom(), 0D);
		Assert.assertEquals(0.5D, viewer.zoom(0D).getZoom(), 0D);
		Assert.assertSame(viewer, viewer.rotationYawRange(() -> -5D, () -> 5D).rotationPitchRange(() -> -2D, () -> 2D));
		Assert.assertEquals(5D, viewer.getMaxRotationYaw(), 0D);
		Assert.assertEquals(-2D, viewer.getMinRotationPitch(), 0D);
	}

	@Test
	public void easesItsDrawnZoomTowardsItsZoom() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel());
		this.bridges.open(new NodeUI(viewer)).frame();
		viewer.zoom(1.5D);
		this.bridges.frame();
		Assert.assertTrue(viewer.getDrawnZoom() > 1D && viewer.getDrawnZoom() < 1.5D);
		this.bridges.frames(600);
		Assert.assertEquals(1.5D, viewer.getDrawnZoom(), 1E-6D);
	}

	@Test
	public void zoomsWithTheWheelOverIt() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel());
		this.bridges.open(new NodeUI(viewer)).frame();
		this.bridges.move(200D, 200D).frames(2).scroll(1D);
		Assert.assertEquals(1.04D, viewer.getZoom(), 1E-9D);
		this.bridges.move(1000D, 200D).frames(2).scroll(1D);
		Assert.assertEquals(1.04D, viewer.getZoom(), 1E-9D);
	}

	@Test
	public void writesTheWheelIntoItsZoomSignal() {
		final DoubleSignal zoom = new DoubleSignal(1D);
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).zoom(zoom).model(new FixedModel());
		this.bridges.open(new NodeUI(viewer)).frame();
		this.bridges.move(200D, 200D).frames(2).scroll(1D);
		Assert.assertEquals(1.04D, zoom.peek(), 1E-9D);
		zoom.set(1.5D);
		Assert.assertEquals(1.5D, viewer.getZoom(), 0D);
	}

	@Test
	public void zoomsWithTheWheelAfterAWriteOfItsSignalItNeverSaw() {
		final DoubleSignal zoom = new DoubleSignal(1D);
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).zoom(zoom).model(new FixedModel());
		this.bridges.open(new NodeUI(viewer)).frame();
		this.bridges.move(200D, 200D).frames(2);
		zoom.silent(signal -> signal.set(1.5D));
		this.bridges.scroll(1D).frames(2);
		Assert.assertEquals(1.04D, viewer.getZoom(), 1E-9D);
		Assert.assertEquals(1.04D, zoom.peek(), 1E-9D);
	}

	@Test
	public void writesItsZoomRangeBackIntoItsSignal() {
		final DoubleSignal zoom = new DoubleSignal(5D);
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).zoom(zoom);
		Assert.assertEquals(2D, viewer.getZoom(), 0D);
		Assert.assertEquals(2D, zoom.peek(), 0D);
	}

	@Test
	public void takesBackTheZoomOfItsSignalOnceItsRangeAllowsIt() {
		final DoubleSignal zoom = new DoubleSignal(8D);
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).zoomRange(1D, 5D).zoom(zoom).model(new FixedModel());
		this.bridges.open(new NodeUI(viewer)).frame();
		Assert.assertEquals(5D, viewer.getZoom(), 0D);
		Assert.assertEquals(5D, zoom.peek(), 0D);
		viewer.zoomRange(1D, 10D);
		this.bridges.frame();
		Assert.assertEquals(8D, viewer.getZoom(), 0D);
		Assert.assertEquals(8D, zoom.peek(), 0D);
	}

	@Test
	public void writesItsRotationRangeBackIntoItsSignal() {
		final DoubleSignal yaw = new DoubleSignal(0D);
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).rotationYawRange(-5D, 5D).rotationYaw(yaw).model(new FixedModel());
		this.bridges.open(new NodeUI(viewer)).frame();
		yaw.set(10D);
		Assert.assertEquals(5D, yaw.peek(), 0D);
		yaw.set(20D);
		this.bridges.frames(2);
		Assert.assertEquals(5D, viewer.getRotationYaw(), 0D);
		Assert.assertEquals(5D, yaw.peek(), 0D);
	}

	@Test
	public void forgetsItsZoomSignalOnceGivenAValue() {
		final DoubleSignal zoom = new DoubleSignal(1.5D);
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).zoom(zoom).zoom(0.8D).model(new FixedModel());
		this.bridges.open(new NodeUI(viewer)).frame();
		zoom.set(1.2D);
		Assert.assertEquals(0.8D, viewer.getZoom(), 0D);
		this.bridges.move(200D, 200D).frames(2).scroll(1D);
		Assert.assertEquals(0.84D, viewer.getZoom(), 1E-9D);
		Assert.assertEquals(1.2D, zoom.peek(), 0D);
	}

	@Test
	public void zoomsLikeTheWheelThroughItsGesture() {
		final List<Object> events = new ArrayList<>();
		final ModelViewerNode viewer = ModelViewerNode
		.create(100D, 100D, 200D, 200D)
		.beforeZoom(event -> events.add("before " + event.getValue()))
		.onZoom(event -> events.add("after " + event.getPrevious() + " " + event.getValue()))
		.model(new FixedModel());
		this.bridges.open(new NodeUI(viewer)).frame();
		Assert.assertSame(viewer, viewer.zoomTo(1.5D));
		viewer.zoom(1.2D);
		Assert.assertEquals(Arrays.asList("before 1.5", "after 1.0 1.5", "after 1.5 1.2"), events);
	}

	@Test
	public void keepsItsZoomWhenItsGestureIsRefused() {
		final DoubleSignal zoom = new DoubleSignal(1D);
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).zoom(zoom).beforeZoom(event -> event.cancel()).model(new FixedModel());
		this.bridges.open(new NodeUI(viewer)).frame();
		viewer.zoomTo(1.5D);
		this.bridges.move(200D, 200D).frames(2).scroll(1D);
		Assert.assertEquals(1D, viewer.getZoom(), 0D);
		Assert.assertEquals(1D, zoom.peek(), 0D);
		zoom.set(1.5D);
		Assert.assertEquals(1.5D, viewer.getZoom(), 0D);
	}

	@Test
	public void stopsTurningWhenDetached() {
		final ModelViewerNode viewer = this.drag(ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel()));
		Assert.assertTrue(ModelViewerNodeTest.isDragged(viewer));
		viewer.fireDetach();
		Assert.assertFalse(ModelViewerNodeTest.isDragged(viewer));
	}

	@Test
	public void turnsWithTheDraggedMouse() {
		final ModelViewerNode viewer = this.drag(ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel()));
		this.bridges.drag(250D, 180D).frame();
		Assert.assertTrue(ModelViewerNodeTest.isDragged(viewer));
		Assert.assertEquals(10D, viewer.getRotationYaw(), 1E-9D);
		Assert.assertEquals(4D, viewer.getRotationPitch(), 1E-9D);
		Assert.assertTrue(viewer.getDrawnRotationYaw() > 0D && viewer.getDrawnRotationYaw() < 10D);
		this.bridges.frames(600);
		Assert.assertEquals(10D, viewer.getDrawnRotationYaw(), 0D);
		Assert.assertEquals(4D, viewer.getDrawnRotationPitch(), 0D);
	}

	@Test
	public void writesTheDragIntoItsRotationSignals() {
		final DoubleSignal yaw = new DoubleSignal(0D);
		final DoubleSignal pitch = new DoubleSignal(0D);
		final ModelViewerNode viewer = this.drag(ModelViewerNode.create(100D, 100D, 200D, 200D).rotationYaw(yaw).rotationPitch(pitch).model(new FixedModel()));
		this.bridges.drag(250D, 180D).frame();
		Assert.assertEquals(10D, yaw.peek(), 1E-9D);
		Assert.assertEquals(4D, pitch.peek(), 1E-9D);
		yaw.set(90D);
		Assert.assertEquals(90D, viewer.getRotationYaw(), 0D);
		Assert.assertEquals(4D, viewer.getRotationPitch(), 1E-9D);
	}

	@Test
	public void keepsAWriteOfItsPitchSignalItNeverSawWhileDraggedOnlyInYaw() {
		final DoubleSignal yaw = new DoubleSignal(0D);
		final DoubleSignal pitch = new DoubleSignal(0D);
		final ModelViewerNode viewer = this.drag(ModelViewerNode.create(100D, 100D, 200D, 200D).rotationYaw(yaw).rotationPitch(pitch).model(new FixedModel()));
		this.bridges.move(250D, 200D).frame();
		pitch.silent(signal -> signal.set(30D));
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(2);
		Assert.assertEquals(10D, yaw.peek(), 1E-9D);
		Assert.assertEquals(30D, viewer.getRotationPitch(), 0D);
		Assert.assertEquals(30D, pitch.peek(), 0D);
	}

	@Test
	public void keepsTheWriteOfAListenerOfOneOfItsRotationSignalsIntoTheOther() {
		final DoubleSignal yaw = new DoubleSignal(0D);
		final DoubleSignal pitch = new DoubleSignal(0D);
		final DoubleSignal otherYaw = new DoubleSignal(0D);
		final DoubleSignal otherPitch = new DoubleSignal(0D);
		final ModelViewerNode viewer = ModelViewerNode.create(0D, 0D, 100D, 100D).rotationYaw(yaw).rotationPitch(pitch).model(new FixedModel());
		final ModelViewerNode other = ModelViewerNode.create(100D, 0D, 100D, 100D).rotationYaw(otherYaw).rotationPitch(otherPitch).model(new FixedModel());
		yaw.onChange(value -> pitch.set(5D));
		otherPitch.onChange(value -> otherYaw.set(5D));
		this.bridges.open(new NodeUI(viewer)).open(new NodeUI(other)).frame();
		viewer.rotate(8D, 25D);
		other.rotate(8D, 25D);
		this.bridges.frames(3);
		Assert.assertEquals(8D, viewer.getRotationYaw(), 0D);
		Assert.assertEquals(5D, viewer.getRotationPitch(), 0D);
		Assert.assertEquals(5D, pitch.peek(), 0D);
		Assert.assertEquals(5D, other.getRotationYaw(), 0D);
		Assert.assertEquals(25D, other.getRotationPitch(), 0D);
		Assert.assertEquals(5D, otherYaw.peek(), 0D);
	}

	@Test
	public void turnsLikeTheMouseThroughItsGesture() {
		final List<Object> events = new ArrayList<>();
		final ModelViewerNode viewer = ModelViewerNode
		.create(100D, 100D, 200D, 200D)
		.beforeRotate(event -> events.add(event.getValue()))
		.onRotate(event -> events.add(event.getPrevious()))
		.model(new FixedModel());
		this.bridges.open(new NodeUI(viewer)).frame();
		Assert.assertSame(viewer, viewer.rotate(30D, 20D));
		viewer.rotationPitch(10D);
		Assert.assertEquals(Arrays.asList(Point.of(30D, 20D), Point.of(0D, 0D), Point.of(30D, 20D)), events);
		Assert.assertEquals(10D, viewer.getRotationPitch(), 0D);
	}

	@Test
	public void keepsItsRotationWhenItsGestureIsRefused() {
		final ModelViewerNode viewer = this.drag(ModelViewerNode.create(100D, 100D, 200D, 200D).beforeRotate(event -> event.cancel()).model(new FixedModel()));
		this.bridges.drag(250D, 180D).frame();
		viewer.rotate(30D, 20D);
		Assert.assertEquals(0D, viewer.getRotationYaw(), 0D);
		Assert.assertEquals(0D, viewer.getRotationPitch(), 0D);
		viewer.rotationYaw(45D);
		Assert.assertEquals(45D, viewer.getRotationYaw(), 0D);
	}

	@Test
	public void keepsItsRotationInsideItsRanges() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel());
		Assert.assertSame(viewer, viewer.rotationYawRange(-5D, 5D).rotationPitchRange(-2D, 2D));
		this.drag(viewer);
		this.bridges.drag(300D, 300D).frame();
		Assert.assertEquals(5D, viewer.getRotationYaw(), 0D);
		Assert.assertEquals(-2D, viewer.getRotationPitch(), 0D);
		Assert.assertEquals(-5D, viewer.getMinRotationYaw(), 0D);
		Assert.assertEquals(2D, viewer.getMaxRotationPitch(), 0D);
	}

	@Test
	public void stopsTurningOnceReleased() {
		final ModelViewerNode viewer = this.drag(ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel()));
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.drag(250D, 180D).frame();
		Assert.assertFalse(ModelViewerNodeTest.isDragged(viewer));
		Assert.assertEquals(0D, viewer.getRotationYaw(), 0D);
	}

	@Test
	public void ignoresAPressBesideIt() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel());
		this.bridges.open(new NodeUI(viewer)).frame();
		this.bridges.move(1000D, 200D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertFalse(ModelViewerNodeTest.isDragged(viewer));
	}

	@Test
	public void turnsOnlyWithTheLeftButton() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel());
		this.bridges.open(new NodeUI(viewer)).frame();
		this.bridges.move(200D, 200D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.RIGHT);
		Assert.assertFalse(ModelViewerNodeTest.isDragged(viewer));
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.getUi().mouseReleased(MouseButton.RIGHT);
		Assert.assertTrue(ModelViewerNodeTest.isDragged(viewer));
	}

	@Test
	public void refusesToBeAttachedWithoutModel() {
		try {
			this.bridges.open(new NodeUI(ModelViewerNode.create(100D, 100D, 200D, 200D)));
			Assert.fail("A model viewer without model cannot be attached");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The ModelViewerNode has no model, call model(...) before attaching it", expected.getMessage());
		}
	}

	private ModelViewerNode drag(final ModelViewerNode viewer) {
		this.bridges.open(new NodeUI(viewer)).frame();
		this.bridges.move(200D, 200D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		return viewer;
	}

	private static boolean isDragged(final ModelViewerNode viewer) {
		try {
			final Field field = ModelViewerNode.class.getDeclaredField("dragged");
			field.setAccessible(true);
			return field.getBoolean(viewer);
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

	@AllArgsConstructor
	public static final class NodeUI extends UI {

		private final Node node;

		@Override
		public void init() {
			super.add(this.node);
		}

	}

	public static final class FixedModel implements IDrawableModel {

		@Override
		public void render() {}

		@Override
		public double getDepth() {
			return 2D;
		}

		@Override
		public double getWidth() {
			return 2D;
		}

		@Override
		public double getHeight() {
			return 2D;
		}

	}

}