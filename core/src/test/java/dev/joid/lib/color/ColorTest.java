package dev.joid.lib.color;

import javax.vecmath.Vector4f;

import org.junit.Assert;
import org.junit.Test;

public class ColorTest {

	@Test
	public void copiesAGradientWithItsEnds() {
		final Color gradient = Color.gradient(new Color(1F, 0F, 0F, 1F), new Color(0F, 0F, 1F, 0.5F), new Vector4f(0F, 0F, 1F, 0F));
		final Color copy = gradient.copy();
		Assert.assertTrue(copy.isGradient());
		Assert.assertNotSame(gradient.gradient, copy.gradient);
		Assert.assertNotSame(gradient.gradient.getStartColor(), copy.gradient.getStartColor());
		Assert.assertEquals(0.5F, copy.gradient.getEndColor().a, 0F);
		Assert.assertEquals(1F, copy.gradient.getDirection().z, 0F);
	}

	@Test
	public void fadesAGradientWithoutLosingIt() {
		final Color gradient = Color.gradient(new Color(1F, 0F, 0F, 1F), new Color(0F, 0F, 1F, 0.5F), new Vector4f(0F, 0F, 1F, 0F));
		final Color faded = gradient.copyAlpha(0.4F);
		Assert.assertTrue(faded.isGradient());
		Assert.assertEquals(0.4F, faded.a, 0F);
		Assert.assertEquals(0.4F, faded.gradient.getStartColor().a, 1E-6F);
		Assert.assertEquals(0.2F, faded.gradient.getEndColor().a, 1E-6F);
		Assert.assertEquals(1F, faded.gradient.getStartColor().r, 0F);
		Assert.assertEquals(1F, faded.gradient.getEndColor().b, 0F);
		Assert.assertEquals(1F, gradient.gradient.getStartColor().a, 0F);
	}

	@Test
	public void fadesAGradientThatStartsTransparent() {
		final Color gradient = Color.gradient(new Color(1F, 0F, 0F, 0F), new Color(0F, 0F, 1F, 1F), new Vector4f(0F, 0F, 1F, 0F));
		final Color faded = gradient.copyAlpha(0.5F);
		Assert.assertEquals(0.5F, faded.gradient.getStartColor().a, 0F);
		Assert.assertEquals(0.5F, faded.gradient.getEndColor().a, 0F);
	}

	@Test
	public void keepsAPlainColorPlain() {
		final Color faded = new Color(1F, 0.5F, 0.25F, 1F).copyAlpha(0.3F);
		Assert.assertFalse(faded.isGradient());
		Assert.assertEquals(0.3F, faded.a, 0F);
		Assert.assertEquals(0.5F, faded.g, 0F);
		Assert.assertFalse(new Color(1F, 0.5F, 0.25F, 1F).copy().isGradient());
	}

}