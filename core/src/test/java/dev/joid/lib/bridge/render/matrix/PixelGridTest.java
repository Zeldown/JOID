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
	public void keepsTheFarEdgeOnePixelAway() {
		final PixelGrid grid = PixelGridTest.grid(new MatrixStack(), 1366, 768);
		for (final double value : new double[] {10D, 10.2D, 10.5D, 10.8D}) {
			Assert.assertEquals(1D, Math.abs(grid.toScreenX(grid.snapRight(value, value + 1D)) - grid.toScreenX(grid.snapX(value))), 1E-4D);
			Assert.assertEquals(1D, Math.abs(grid.toScreenY(grid.snapBottom(value, value + 1D)) - grid.toScreenY(grid.snapY(value))), 1E-4D);
			Assert.assertEquals(1D, Math.abs(grid.toScreenX(grid.snapRight(value, value - 1D)) - grid.toScreenX(grid.snapX(value))), 1E-4D);
		}
	}

	@Test
	public void snapsTheFarEdgeLikeTheNearOne() {
		final PixelGrid grid = PixelGridTest.grid(new MatrixStack(), 1366, 768);
		Assert.assertEquals(grid.snapX(110.3D), grid.snapRight(10D, 110.3D), 0D);
		Assert.assertEquals(grid.snapY(110.3D), grid.snapBottom(10D, 110.3D), 0D);
		Assert.assertEquals(grid.snapX(10D), grid.snapRight(10D, 10D), 0D);
		Assert.assertEquals(grid.snapY(10D), grid.snapBottom(10D, 10D), 0D);
	}

	@Test
	public void keepsTheThicknessOfAStrokeEverywhere() {
		final PixelGrid grid = PixelGridTest.grid(new MatrixStack(), 1366, 768);
		for (final double value : new double[] {10D, 10.2D, 10.5D, 10.8D, 133.3D}) {
			Assert.assertEquals(2D, Math.abs(grid.toScreenX(grid.snapWidth(value, 3D)) - grid.toScreenX(grid.snapX(value))), 1E-4D);
			Assert.assertEquals(2D, Math.abs(grid.toScreenY(grid.snapHeight(value, 3D)) - grid.toScreenY(grid.snapY(value))), 1E-4D);
			Assert.assertEquals(1D, Math.abs(grid.toScreenY(grid.snapHeight(value, 1.1D)) - grid.toScreenY(grid.snapY(value))), 1E-4D);
			Assert.assertEquals(1D, Math.abs(grid.toScreenX(grid.snapWidth(value, 0.2D)) - grid.toScreenX(grid.snapX(value))), 1E-4D);
		}
	}

	@Test
	public void drawsAThinSpanAsALineOfConstantThickness() {
		final PixelGrid grid = PixelGridTest.grid(new MatrixStack(), 1366, 768);
		for (final double value : new double[] {10D, 10.2D, 10.5D, 10.8D, 133.3D}) {
			final PixelGrid.Span span = grid.spanY(value, 3D);
			Assert.assertEquals(2D, Math.abs(grid.toScreenY(span.getEnd()) - grid.toScreenY(span.getStart())), 1E-4D);
			Assert.assertEquals(Math.rint(grid.toScreenY(span.getStart())), grid.toScreenY(span.getStart()), 1E-4D);
			Assert.assertEquals(1F, span.getCoverage(), 0F);
		}
	}

	@Test
	public void lightensASpanThinnerThanAPixel() {
		final PixelGrid grid = PixelGridTest.grid(new MatrixStack(), 960, 540);
		final PixelGrid.Span span = grid.spanX(10.3D, 1.1D);
		Assert.assertEquals(1D, Math.abs(grid.toScreenX(span.getEnd()) - grid.toScreenX(span.getStart())), 1E-4D);
		Assert.assertEquals(0.55F, span.getCoverage(), 1E-4F);
	}

	@Test
	public void snapsTheEdgesOfAWideSpan() {
		final PixelGrid grid = PixelGridTest.grid(new MatrixStack(), 1366, 768);
		final PixelGrid.Span span = grid.spanX(10.3D, 100D);
		Assert.assertEquals(grid.snapX(10.3D), span.getStart(), 0D);
		Assert.assertEquals(grid.snapX(110.3D), span.getEnd(), 0D);
		Assert.assertEquals(1F, span.getCoverage(), 0F);
	}

	@Test
	public void growsAStrokeFromItsAnchor() {
		final PixelGrid grid = PixelGridTest.grid(new MatrixStack(), 1366, 768);
		Assert.assertTrue(grid.snapWidth(10.3D, -3D) < grid.snapX(10.3D));
		Assert.assertTrue(grid.snapHeight(10.3D, -3D) < grid.snapY(10.3D));
		Assert.assertEquals(10.3D, grid.snapWidth(10.3D, 0D), 0D);
	}

	@Test
	public void quantizesAMotionToWholePixels() {
		final PixelGrid grid = PixelGridTest.grid(new MatrixStack(), 1366, 768);
		for (final double motion : new double[] {0.3D, 1D, -2.6D, 133.3D}) {
			final double screenX = grid.toScreenX(grid.quantizeX(motion)) - grid.toScreenX(0D);
			final double screenY = grid.toScreenY(grid.quantizeY(motion)) - grid.toScreenY(0D);
			Assert.assertEquals(Math.rint(screenX), screenX, 1E-4D);
			Assert.assertEquals(Math.rint(screenY), screenY, 1E-4D);
			Assert.assertEquals(motion, grid.quantizeX(motion), 0.5D / grid.getScaleX() + 1E-4D);
		}
	}

	@Test
	public void leavesARotatedTransformExact() {
		final MatrixStack modelView = new MatrixStack();
		modelView.translate(0.3D, 0.6D, 0D);
		modelView.rotate(30D, 0D, 0D, 1D);
		final PixelGrid grid = PixelGridTest.grid(modelView, 1920, 1080);
		Assert.assertEquals(0.3D, grid.quantizeX(0.3D), 0D);
		Assert.assertEquals(0.3D, grid.quantizeY(0.3D), 0D);
		Assert.assertEquals(10.2D, grid.snapRight(10D, 10.2D), 0D);
		Assert.assertEquals(10.2D, grid.snapBottom(10D, 10.2D), 0D);
		Assert.assertEquals(10.2D, grid.spanX(10D, 0.2D).getEnd(), 0D);
		Assert.assertEquals(1F, grid.spanX(10D, 0.2D).getCoverage(), 0F);
	}

	@Test
	public void neverAlignsAFlattenedTransform() {
		final MatrixStack modelView = new MatrixStack();
		modelView.scale(0D, 0D, 1D);
		final PixelGrid grid = PixelGridTest.grid(modelView, 1920, 1080);
		Assert.assertFalse(grid.isAligned());
		Assert.assertEquals(2.5D, grid.quantizeX(2.5D), 0D);
		Assert.assertEquals(2.5D, grid.snapX(2.5D), 0D);
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

	@Test
	public void alignsAPanelFacingAPerspectiveCamera() {
		final MatrixStack modelView = new MatrixStack();
		modelView.translate(0D, 0D, -10D);
		Assert.assertTrue(PixelGrid.of(PixelGridTest.perspective(), modelView.getMatrix(), 1920, 1080).isAligned());
	}

	@Test
	public void leavesATiltedPerspectivePanelExact() {
		final MatrixStack modelView = new MatrixStack();
		modelView.translate(0D, 0D, -10D);
		modelView.rotate(60D, 1D, 0D, 0D);
		final PixelGrid grid = PixelGrid.of(PixelGridTest.perspective(), modelView.getMatrix(), 1920, 1080);
		Assert.assertFalse(grid.isAligned());
		Assert.assertEquals(0.3D, grid.snapY(0.3D), 0D);
		Assert.assertEquals(0.3D, grid.quantizeY(0.3D), 0D);
	}

	private static float[] perspective() {
		final float near = 0.1F;
		final float far = 100F;
		return new float[] {9F / 16F, 0F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 0F, (far + near) / (near - far), -1F, 0F, 0F, 2F * far * near / (near - far), 0F};
	}

	private static PixelGrid grid(final MatrixStack modelView, final int width, final int height) {
		final MatrixStack projection = new MatrixStack();
		projection.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		return PixelGrid.of(projection.getMatrix(), modelView.getMatrix(), width, height);
	}

}