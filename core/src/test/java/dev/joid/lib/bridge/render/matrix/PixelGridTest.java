package dev.joid.lib.bridge.render.matrix;

import org.junit.Assert;
import org.junit.Test;

public class PixelGridTest {

	@Test
	public void doublesOnA4kWindow() {
		final PixelGrid grid = PixelGridTest.grid(new MatrixStack(), 3840, 2160);
		Assert.assertEquals(2D, grid.getScaleX(), 1E-4D);
		Assert.assertEquals(2D, grid.getScaleY(), 1E-4D);
	}

	@Test
	public void roundsPixelSizesUp() {
		final PixelGrid grid = PixelGrid.of(1.5D, 0.25D);
		Assert.assertEquals(15, grid.toPixelWidth(10D));
		Assert.assertEquals(16, grid.toPixelWidth(10.1D));
		Assert.assertEquals(3, grid.toPixelHeight(10D));
		Assert.assertEquals(1, grid.toPixelHeight(0D));
	}

	@Test
	public void locatesTheScreenPixels() {
		final MatrixStack modelView = new MatrixStack();
		modelView.translate(100.5D, 20D, 0D);
		final PixelGrid grid = PixelGridTest.grid(modelView, 1920, 1080);
		Assert.assertEquals(110.5D, grid.toScreenX(10D), 1E-4D);
		Assert.assertEquals(1080D - 30D, grid.toScreenY(10D), 1E-4D);
		Assert.assertEquals(10D, grid.fromScreenX(110.5D), 1E-4D);
		Assert.assertEquals(10D, grid.fromScreenY(1050D), 1E-4D);
	}

	@Test
	public void followsANonUniformScale() {
		final MatrixStack modelView = new MatrixStack();
		modelView.scale(2D, 3D, 1D);
		final PixelGrid grid = PixelGridTest.grid(modelView, 1920, 1080);
		Assert.assertEquals(2D, grid.getScaleX(), 1E-4D);
		Assert.assertEquals(3D, grid.getScaleY(), 1E-4D);
	}

	@Test
	public void mapsTheCanvasOntoTheWindow() {
		final PixelGrid grid = PixelGridTest.grid(new MatrixStack(), 1920, 1080);
		Assert.assertEquals(1D, grid.getScaleX(), 1E-4D);
		Assert.assertEquals(1D, grid.getScaleY(), 1E-4D);
		Assert.assertTrue(grid.isAligned());
	}

	@Test
	public void snapsOntoWholeScreenPixels() {
		final MatrixStack modelView = new MatrixStack();
		modelView.translate(0.3D, 0.6D, 0D);
		modelView.scale(0.75D, 0.75D, 1D);
		final PixelGrid grid = PixelGridTest.grid(modelView, 1920, 1080);
		for (final double value : new double[] {0D, 10D, 10.4D, 133.3D}) {
			final double screenX = grid.toScreenX(grid.snapX(value));
			final double screenY = grid.toScreenY(grid.snapY(value));
			Assert.assertEquals(Math.rint(screenX), screenX, 1E-4D);
			Assert.assertEquals(Math.rint(screenY), screenY, 1E-4D);
			Assert.assertTrue(Math.abs(grid.snapX(value) - value) <= 0.5D / 0.75D + 1E-4D);
		}
	}

	@Test
	public void keepsItsScaleUnderARotation() {
		final MatrixStack modelView = new MatrixStack();
		modelView.translate(300D, -40D, 0D);
		modelView.rotate(45D, 0D, 0D, 1D);
		modelView.scale(1.5D, 1.5D, 1D);
		final PixelGrid grid = PixelGridTest.grid(modelView, 1920, 1080);
		Assert.assertEquals(1.5D, grid.getScaleX(), 1E-4D);
		Assert.assertEquals(1.5D, grid.getScaleY(), 1E-4D);
		Assert.assertFalse(grid.isAligned());
		Assert.assertEquals(10.25D, grid.snapX(10.25D), 0D);
	}

	private static PixelGrid grid(final MatrixStack modelView, final int width, final int height) {
		final MatrixStack projection = new MatrixStack();
		projection.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		return PixelGrid.of(projection.getMatrix(), modelView.getMatrix(), width, height);
	}

}