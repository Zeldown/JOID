package be.zeldown.joid.lib.shader.pipeline.dto;

import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.lib.bridge.render.matrix.MatrixStack;
import be.zeldown.joid.lib.bridge.render.matrix.PixelGrid;

public class ShaderPassContextTest {

	@Test
	public void keepsTheDrawnArea() {
		final PixelGrid grid = PixelGrid.of(1D, 1D);
		final ShaderPassContext context = ShaderPassContext.create(10D, 20D, 100D, 50D, 5D, grid);
		Assert.assertEquals(10D, context.getX(), 0D);
		Assert.assertEquals(20D, context.getY(), 0D);
		Assert.assertEquals(100D, context.getWidth(), 0D);
		Assert.assertEquals(50D, context.getHeight(), 0D);
		Assert.assertEquals(5D, context.getExpansion(), 0D);
		Assert.assertSame(grid, context.getGrid());
	}

	@Test
	public void alignsTheTextureOnScreenPixels() {
		final ShaderPassContext context = ShaderPassContext.create(10.3D, 20.6D, 100D, 50D, 5D, PixelGrid.of(2D, 0.5D));
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
		final ShaderPassContext context = ShaderPassContext.create(10.3D, 20.6D, 100D, 50D, 5D, PixelGrid.of(projection.getMatrix(), modelView.getMatrix(), 3840, 2160));
		Assert.assertEquals(5.3D, context.getRegionX(), 1E-9D);
		Assert.assertEquals(15.6D, context.getRegionY(), 1E-9D);
		Assert.assertEquals(110D, context.getRegionWidth(), 1E-9D);
		Assert.assertEquals(60D, context.getRegionHeight(), 1E-9D);
		Assert.assertEquals(220, context.getTextureWidth());
		Assert.assertEquals(120, context.getTextureHeight());
	}

}