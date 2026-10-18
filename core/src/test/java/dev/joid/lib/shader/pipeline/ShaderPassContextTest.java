package dev.joid.lib.shader.pipeline;

import java.lang.reflect.Constructor;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.matrix.MatrixStack;
import dev.joid.lib.bridge.render.matrix.PixelGrid;

public class ShaderPassContextTest {

	@Test
	public void keepsTheDrawnArea() {
		final PixelGrid grid = ShaderPassContextTest.grid(1D, 1D);
		final ShaderPassContext context = ShaderPassContext.of(10D, 20D, 100D, 50D, 5D, 0D, grid);
		Assert.assertEquals(10D, context.getX(), 0D);
		Assert.assertEquals(20D, context.getY(), 0D);
		Assert.assertEquals(100D, context.getWidth(), 0D);
		Assert.assertEquals(50D, context.getHeight(), 0D);
		Assert.assertEquals(5D, context.getExpansion(), 0D);
		Assert.assertSame(grid, context.getGrid());
	}

	@Test
	public void alignsTheTextureOnScreenPixels() {
		final ShaderPassContext context = ShaderPassContext.of(10.3D, 20.6D, 100D, 50D, 5D, 0D, ShaderPassContextTest.grid(2D, 0.5D));
		Assert.assertEquals(5D, context.getRegionX(), 1E-9D);
		Assert.assertEquals(14D, context.getRegionY(), 1E-9D);
		Assert.assertEquals(110.5D, context.getRegionWidth(), 1E-9D);
		Assert.assertEquals(62D, context.getRegionHeight(), 1E-9D);
		Assert.assertEquals(221, context.getTextureWidth());
		Assert.assertEquals(31, context.getTextureHeight());
		Assert.assertEquals(1F / 221F, context.getTexelWidth(), 0F);
		Assert.assertEquals(1F / 31F, context.getTexelHeight(), 0F);
	}

	@Test
	public void keepsTheExpandedAreaOfARotatedDraw() {
		final MatrixStack projection = new MatrixStack();
		final MatrixStack modelView = new MatrixStack();
		projection.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		modelView.rotate(30D, 0D, 0D, 1D);
		final ShaderPassContext context = ShaderPassContext.of(10.3D, 20.6D, 100D, 50D, 5D, 0D, PixelGrid.of(projection.getMatrix(), modelView.getMatrix(), 3840, 2160));
		Assert.assertEquals(5.3D, context.getRegionX(), 1E-9D);
		Assert.assertEquals(15.6D, context.getRegionY(), 1E-9D);
		Assert.assertEquals(110D, context.getRegionWidth(), 1E-9D);
		Assert.assertEquals(60D, context.getRegionHeight(), 1E-9D);
		Assert.assertEquals(220, context.getTextureWidth());
		Assert.assertEquals(120, context.getTextureHeight());
	}

	@Test
	public void clipsTheTextureToTheViewAndItsMargin() {
		final ShaderPassContext context = ShaderPassContext.of(-1000D, -500D, 4000D, 2000D, 5D, 10D, ShaderPassContextTest.view(new MatrixStack()));
		Assert.assertEquals(-10D, context.getRegionX(), 1E-3D);
		Assert.assertEquals(-10D, context.getRegionY(), 1E-3D);
		Assert.assertEquals(1940D, context.getRegionWidth(), 1E-3D);
		Assert.assertEquals(1100D, context.getRegionHeight(), 1E-3D);
		Assert.assertEquals(1940, context.getTextureWidth());
		Assert.assertEquals(1100, context.getTextureHeight());
		Assert.assertEquals(-1000D, context.getX(), 0D);
		Assert.assertEquals(4000D, context.getWidth(), 0D);
	}

	@Test
	public void keepsAOnePixelStripOfAnAreaOutsideTheView() {
		final ShaderPassContext context = ShaderPassContext.of(3000D, 20D, 100D, 50D, 0D, 10D, ShaderPassContextTest.view(new MatrixStack()));
		Assert.assertEquals(3000D, context.getRegionX(), 1E-3D);
		Assert.assertEquals(1D, context.getRegionWidth(), 1E-3D);
		Assert.assertEquals(1, context.getTextureWidth());
		Assert.assertEquals(50, context.getTextureHeight());
	}

	@Test
	public void clipsARotatedAreaOnItsOwnTexels() {
		final MatrixStack modelView = new MatrixStack();
		modelView.rotate(30D, 0D, 0D, 1D);
		final ShaderPassContext context = ShaderPassContext.of(-5000D, -5000D, 10000D, 10000D, 0D, 0D, ShaderPassContextTest.view(modelView));
		Assert.assertTrue(context.getTextureWidth() < 3000);
		Assert.assertTrue(context.getTextureHeight() < 3000);
		Assert.assertEquals(context.getTextureWidth(), context.getRegionWidth(), 1E-3D);
		Assert.assertEquals(context.getTextureHeight(), context.getRegionHeight(), 1E-3D);
		Assert.assertEquals(Math.rint(context.getRegionX()), context.getRegionX(), 1E-3D);
		Assert.assertEquals(Math.rint(context.getRegionY()), context.getRegionY(), 1E-3D);
	}

	private static PixelGrid view(final MatrixStack modelView) {
		final MatrixStack projection = new MatrixStack();
		projection.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		return PixelGrid.of(projection.getMatrix(), modelView.getMatrix(), 1920, 1080);
	}

	private static PixelGrid grid(final double scaleX, final double scaleY) {
		try {
			final Constructor<PixelGrid> constructor = PixelGrid.class.getDeclaredConstructor(double.class, double.class, double.class, double.class, double.class, double.class, double.class, double.class, int.class, int.class, boolean.class, boolean.class);
			constructor.setAccessible(true);
			return constructor.newInstance(scaleX, scaleY, 0D, 0D, scaleX, scaleY, 0D, 0D, 10000, 10000, true, true);
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

}