package dev.joid.lib.ui.node.impl.design.model;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.draw.model.IDrawableModel;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import lombok.AllArgsConstructor;

public class ModelViewerNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void startsAtItsNaturalSizeWithoutLimits() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D);
		Assert.assertEquals(1D, viewer.getTargetSize(), 0D);
		Assert.assertEquals(0.1D, viewer.getMinSize(), 0D);
		Assert.assertEquals(2D, viewer.getMaxSize(), 0D);
		Assert.assertEquals(-Double.MAX_VALUE, viewer.getMinRotationYaw(), 0D);
		Assert.assertEquals(Double.MAX_VALUE, viewer.getMaxRotationPitch(), 0D);
		Assert.assertFalse(viewer.isDragged());
	}

	@Test
	public void jumpsToAGivenValue() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D);
		Assert.assertSame(viewer, viewer.size(1.5D).rotationYaw(30D).rotationPitch(20D));
		Assert.assertEquals(1.5D, viewer.getSize(), 0D);
		Assert.assertEquals(1.5D, viewer.getTargetSize(), 0D);
		Assert.assertEquals(30D, viewer.getRotationYaw(), 0D);
		Assert.assertEquals(30D, viewer.getTargetRotationYaw(), 0D);
		Assert.assertEquals(20D, viewer.getRotationPitch(), 0D);
		Assert.assertEquals(20D, viewer.getTargetRotationPitch(), 0D);
	}

	@Test
	public void keepsAValueSetFromCodeOnceOpen() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel());
		this.bridges.open(new NodeUI(viewer)).frame();
		viewer.size(1.5D).rotationYaw(30D);
		this.bridges.frames(5);
		Assert.assertEquals(1.5D, viewer.getSize(), 0D);
		Assert.assertEquals(1.5D, viewer.getTargetSize(), 0D);
		Assert.assertEquals(30D, viewer.getRotationYaw(), 0D);
		Assert.assertEquals(30D, viewer.getTargetRotationYaw(), 0D);
	}

	@Test
	public void followsTheLastOfASizeAndAZoom() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D);
		viewer.size(1.5D);
		Assert.assertEquals(1.2D, viewer.zoom(1.2D).getTargetSize(), 0D);
		viewer.size(0.8D);
		Assert.assertEquals(0.8D, viewer.getTargetSize(), 0D);
	}

	@Test
	public void zoomsInsideItsSizeRange() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D);
		Assert.assertEquals(2D, viewer.zoom(5D).getTargetSize(), 0D);
		Assert.assertEquals(0.1D, viewer.zoom(0D).getTargetSize(), 0D);
		Assert.assertSame(viewer, viewer.sizeRange(0.5D, 3D));
		Assert.assertEquals(3D, viewer.zoom(5D).getTargetSize(), 0D);
		Assert.assertEquals(0.5D, viewer.zoom(0D).getTargetSize(), 0D);
		Assert.assertEquals(1.2D, viewer.zoom(1.2D).getTargetSize(), 0D);
	}

	@Test
	public void easesItsSizeTowardsTheZoom() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel());
		this.bridges.open(new NodeUI(viewer)).frame();
		viewer.zoom(1.5D);
		this.bridges.frame();
		Assert.assertTrue(viewer.getSize() > 1D && viewer.getSize() < 1.5D);
		this.bridges.frames(600);
		Assert.assertEquals(1.5D, viewer.getSize(), 1E-6D);
	}

	@Test
	public void zoomsWithTheWheelOverIt() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel());
		this.bridges.open(new NodeUI(viewer)).frame();
		this.bridges.move(200D, 200D).frames(2).scroll(1D);
		Assert.assertEquals(1.04D, viewer.getTargetSize(), 1E-9D);
		this.bridges.move(1000D, 200D).frames(2).scroll(1D);
		Assert.assertEquals(1.04D, viewer.getTargetSize(), 1E-9D);
	}

	@Test
	public void stopsTurningWhenDetached() {
		final ModelViewerNode viewer = this.drag(ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel()));
		Assert.assertTrue(viewer.isDragged());
		viewer.fireDetach();
		Assert.assertFalse(viewer.isDragged());
	}

	@Test
	public void turnsWithTheDraggedMouse() {
		final ModelViewerNode viewer = this.drag(ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel()));
		this.bridges.move(250D, 180D).frame();
		Assert.assertTrue(viewer.isDragged());
		Assert.assertEquals(10D, viewer.getTargetRotationYaw(), 1E-9D);
		Assert.assertEquals(4D, viewer.getTargetRotationPitch(), 1E-9D);
		Assert.assertTrue(viewer.getRotationYaw() > 0D && viewer.getRotationYaw() < 10D);
		this.bridges.frames(600);
		Assert.assertEquals(10D, viewer.getRotationYaw(), 0D);
		Assert.assertEquals(4D, viewer.getRotationPitch(), 0D);
	}

	@Test
	public void keepsItsRotationInsideItsRanges() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel());
		Assert.assertSame(viewer, viewer.rotationYawRange(-5D, 5D).rotationPitchRange(-2D, 2D));
		this.drag(viewer);
		this.bridges.move(300D, 300D).frame();
		Assert.assertEquals(5D, viewer.getTargetRotationYaw(), 0D);
		Assert.assertEquals(-2D, viewer.getTargetRotationPitch(), 0D);
		Assert.assertEquals(-5D, viewer.getMinRotationYaw(), 0D);
		Assert.assertEquals(2D, viewer.getMaxRotationPitch(), 0D);
	}

	@Test
	public void stopsTurningOnceReleased() {
		final ModelViewerNode viewer = this.drag(ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel()));
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.move(250D, 180D).frame();
		Assert.assertFalse(viewer.isDragged());
		Assert.assertEquals(0D, viewer.getTargetRotationYaw(), 0D);
	}

	@Test
	public void ignoresAPressBesideIt() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel());
		this.bridges.open(new NodeUI(viewer)).frame();
		this.bridges.move(1000D, 200D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertFalse(viewer.isDragged());
	}

	@Test
	public void turnsOnlyWithTheLeftButton() {
		final ModelViewerNode viewer = ModelViewerNode.create(100D, 100D, 200D, 200D).model(new FixedModel());
		this.bridges.open(new NodeUI(viewer)).frame();
		this.bridges.move(200D, 200D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.RIGHT);
		Assert.assertFalse(viewer.isDragged());
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.getUi().mouseReleased(MouseButton.RIGHT);
		Assert.assertTrue(viewer.isDragged());
	}

	@Test
	public void staysStillWithoutModel() {
		final ModelViewerNode viewer = this.drag(ModelViewerNode.create(100D, 100D, 200D, 200D));
		this.bridges.move(250D, 180D).frame();
		Assert.assertEquals(0D, viewer.getTargetRotationYaw(), 0D);
	}

	private ModelViewerNode drag(final ModelViewerNode viewer) {
		this.bridges.open(new NodeUI(viewer)).frame();
		this.bridges.move(200D, 200D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		return viewer;
	}

	@AllArgsConstructor
	public static final class NodeUI extends UI {

		private final Node node;

		@Override
		public void init() {
			super.add(this.node);
		}

	}

	private static final class FixedModel implements IDrawableModel {

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