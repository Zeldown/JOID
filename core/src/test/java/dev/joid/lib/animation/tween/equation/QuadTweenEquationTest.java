package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class QuadTweenEquationTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final QuadTweenEquation quad : new QuadTweenEquation[] {QuadTweenEquation.IN, QuadTweenEquation.OUT, QuadTweenEquation.IN_OUT}) {
			Assert.assertEquals(0F, quad.compute(0F), 0F);
			Assert.assertEquals(1F, quad.compute(1F), 0F);
		}
	}

	@Test
	public void acceleratesIn() {
		Assert.assertEquals(0.0625F, QuadTweenEquation.IN.compute(0.25F), 0F);
		Assert.assertEquals(0.25F, QuadTweenEquation.IN.compute(0.5F), 0F);
	}

	@Test
	public void deceleratesOut() {
		Assert.assertEquals(0.4375F, QuadTweenEquation.OUT.compute(0.25F), 0F);
		Assert.assertEquals(0.75F, QuadTweenEquation.OUT.compute(0.5F), 0F);
	}

	@Test
	public void acceleratesThenDeceleratesInOut() {
		Assert.assertEquals(0.125F, QuadTweenEquation.IN_OUT.compute(0.25F), 0F);
		Assert.assertEquals(0.5F, QuadTweenEquation.IN_OUT.compute(0.5F), 0F);
		Assert.assertEquals(0.875F, QuadTweenEquation.IN_OUT.compute(0.75F), 0F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - QuadTweenEquation.IN.compute(1F - t), QuadTweenEquation.OUT.compute(t), 0.0001F);
			Assert.assertEquals(1F - QuadTweenEquation.IN_OUT.compute(1F - t), QuadTweenEquation.IN_OUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void keepsGrowing() {
		for (final QuadTweenEquation quad : new QuadTweenEquation[] {QuadTweenEquation.IN, QuadTweenEquation.OUT, QuadTweenEquation.IN_OUT}) {
			for (int i = 1; i <= 20; i++) {
				Assert.assertTrue(quad.compute(i / 20F) > quad.compute((i - 1) / 20F));
			}
		}
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Quad.IN", QuadTweenEquation.IN.toString());
		Assert.assertEquals("Quad.OUT", QuadTweenEquation.OUT.toString());
		Assert.assertEquals("Quad.IN_OUT", QuadTweenEquation.IN_OUT.toString());
	}

}