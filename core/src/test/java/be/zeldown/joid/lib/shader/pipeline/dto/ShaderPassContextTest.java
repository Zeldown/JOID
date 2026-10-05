package be.zeldown.joid.lib.shader.pipeline.dto;

import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.lib.bridge.render.matrix.PixelScale;

public class ShaderPassContextTest {

	@Test
	public void keepsTheDrawnArea() {
		final PixelScale scale = PixelScale.of(1D, 1D);
		final ShaderPassContext context = ShaderPassContext.create(10D, 20D, 100D, 50D, 5D, scale);
		Assert.assertEquals(10D, context.getX(), 0D);
		Assert.assertEquals(20D, context.getY(), 0D);
		Assert.assertEquals(100D, context.getWidth(), 0D);
		Assert.assertEquals(50D, context.getHeight(), 0D);
		Assert.assertEquals(5D, context.getExpansion(), 0D);
		Assert.assertSame(scale, context.getScale());
	}

	@Test
	public void sizesTheTextureFromThePixelScale() {
		final ShaderPassContext context = ShaderPassContext.create(10D, 20D, 100D, 50D, 5D, PixelScale.of(2D, 0.5D));
		Assert.assertEquals(220, context.getTextureWidth());
		Assert.assertEquals(30, context.getTextureHeight());
		Assert.assertEquals(1F / 220F, context.getTexelWidth(), 0F);
		Assert.assertEquals(1F / 30F, context.getTexelHeight(), 0F);
	}

}