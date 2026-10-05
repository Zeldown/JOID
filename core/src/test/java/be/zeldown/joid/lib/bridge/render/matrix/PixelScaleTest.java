package be.zeldown.joid.lib.bridge.render.matrix;

import org.junit.Assert;
import org.junit.Test;

public class PixelScaleTest {

	@Test
	public void doublesOnA4kWindow() {
		final PixelScale scale = PixelScaleTest.scale(new MatrixStack(), 3840, 2160);
		Assert.assertEquals(2D, scale.getX(), 1E-6D);
		Assert.assertEquals(2D, scale.getY(), 1E-6D);
	}

	@Test
	public void roundsPixelSizesUp() {
		final PixelScale scale = PixelScale.of(1.5D, 0.25D);
		Assert.assertEquals(15, scale.toPixelWidth(10D));
		Assert.assertEquals(16, scale.toPixelWidth(10.1D));
		Assert.assertEquals(3, scale.toPixelHeight(10D));
		Assert.assertEquals(1, scale.toPixelHeight(0D));
	}

	@Test
	public void followsANonUniformScale() {
		final MatrixStack modelView = new MatrixStack();
		modelView.scale(2D, 3D, 1D);
		final PixelScale scale = PixelScaleTest.scale(modelView, 1920, 1080);
		Assert.assertEquals(2D, scale.getX(), 1E-6D);
		Assert.assertEquals(3D, scale.getY(), 1E-6D);
	}

	@Test
	public void mapsTheCanvasOntoTheWindow() {
		final PixelScale scale = PixelScaleTest.scale(new MatrixStack(), 1920, 1080);
		Assert.assertEquals(1D, scale.getX(), 1E-6D);
		Assert.assertEquals(1D, scale.getY(), 1E-6D);
	}

	@Test
	public void ignoresTranslationsAndRotations() {
		final MatrixStack modelView = new MatrixStack();
		modelView.translate(300D, -40D, 0D);
		modelView.rotate(45D, 0D, 0D, 1D);
		modelView.scale(1.5D, 1.5D, 1D);
		final PixelScale scale = PixelScaleTest.scale(modelView, 1920, 1080);
		Assert.assertEquals(1.5D, scale.getX(), 1E-6D);
		Assert.assertEquals(1.5D, scale.getY(), 1E-6D);
	}

	private static PixelScale scale(final MatrixStack modelView, final int width, final int height) {
		final MatrixStack projection = new MatrixStack();
		projection.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		return PixelScale.of(projection.getMatrix(), modelView.getMatrix(), width, height);
	}

}