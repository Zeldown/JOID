package dev.joid.lib.draw.model;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.draw.DrawUtils;
import lombok.RequiredArgsConstructor;

public class DrawModelTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test(expected = RuntimeException.class)
	public void refusesASecondInstance() {
		Assert.assertSame(DrawUtils.MODEL, DrawModel.getInstance());
		new DrawModel();
	}

	@Test
	public void drawsAModelWithTheObjAxesAtItsPosition() {
		final RecordingModel model = new RecordingModel(this.bridges.getRender(), false);
		DrawUtils.MODEL.drawModel(200D, 100D, 30D, model);
		Assert.assertEquals(200D, model.grid.toScreenX(0D), 1E-3D);
		Assert.assertEquals(230D, model.grid.toScreenX(1D), 1E-3D);
		Assert.assertEquals(1080D - 70D, model.grid.toScreenY(1D), 1E-3D);
		Assert.assertEquals(30F, model.depth, 1E-4F);
	}

	@Test
	public void scalesEachAxisOfAModel() {
		final RecordingModel model = new RecordingModel(this.bridges.getRender(), false);
		DrawUtils.MODEL.drawModel(200D, 100D, 10D, 20D, 30D, model);
		Assert.assertEquals(210D, model.grid.toScreenX(1D), 1E-3D);
		Assert.assertEquals(1080D - 80D, model.grid.toScreenY(1D), 1E-3D);
		Assert.assertEquals(30F, model.depth, 1E-4F);
	}

	@Test
	public void litsTheModelOnBothFaces() {
		final RecordingModel model = new RecordingModel(this.bridges.getRender(), false);
		this.bridges.getRender().getState().cull(true);
		DrawUtils.MODEL.drawModel(0D, 0D, 1D, model);
		Assert.assertTrue(model.lighting);
		Assert.assertFalse(model.cull);
		Assert.assertFalse(this.bridges.getRender().getState().isLighting());
	}

	@Test
	public void clearsTheDepthBeforeAndAfterTheModel() {
		final RecordingModel model = new RecordingModel(this.bridges.getRender(), false);
		DrawUtils.MODEL.drawModel(0D, 0D, 1D, model);
		Assert.assertEquals(1, model.depthClears);
		Assert.assertEquals(2, this.bridges.getRender().getDepthClears());
	}

	@Test
	public void restoresTheMatrixWhenTheModelFails() {
		final float[] before = this.bridges.getRender().getModelView().getMatrix().clone();
		try {
			DrawUtils.MODEL.drawModel(50D, 50D, 2D, new RecordingModel(this.bridges.getRender(), true));
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertArrayEquals(before, this.bridges.getRender().getModelView().getMatrix(), 0F);
			Assert.assertFalse(this.bridges.getRender().getState().isLighting());
		}
	}

	@RequiredArgsConstructor
	private static final class RecordingModel implements IDrawableModel {

		private final RecordingRenderBridge render;
		private final boolean               failing;

		private PixelGrid grid;
		private float     depth;
		private boolean   cull;
		private boolean   lighting;
		private int       depthClears;

		@Override
		public void render() {
			this.grid = this.render.getPixelGrid();
			this.depth = this.render.getModelView().getMatrix()[10];
			this.cull = this.render.getState().isCull();
			this.lighting = this.render.getState().isLighting();
			this.depthClears = this.render.getDepthClears();
			if (this.failing) {
				throw new IllegalStateException("Model failed");
			}
		}

		@Override
		public double getDepth() {
			return 1D;
		}

		@Override
		public double getWidth() {
			return 1D;
		}

		@Override
		public double getHeight() {
			return 1D;
		}

	}

}