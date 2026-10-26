package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class CubicTweenEquationTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final CubicTweenEquation cubic : new CubicTweenEquation[] {CubicTweenEquation.IN, CubicTweenEquation.OUT, CubicTweenEquation.IN_OUT}) {
			Assert.assertEquals(0F, cubic.compute(0F), 0F);
			Assert.assertEquals(1F, cubic.compute(1F), 0F);
		}
	}

	@Test
	public void acceleratesIn() {
		Assert.assertEquals(0.015625F, CubicTweenEquation.IN.compute(0.25F), 0F);
		Assert.assertEquals(0.125F, CubicTweenEquation.IN.compute(0.5F), 0F);
	}

	@Test
	public void deceleratesOut() {
		Assert.assertEquals(0.578125F, CubicTweenEquation.OUT.compute(0.25F), 0F);
		Assert.assertEquals(0.875F, CubicTweenEquation.OUT.compute(0.5F), 0F);
	}

	@Test
	public void acceleratesThenDeceleratesInOut() {
		Assert.assertEquals(0.0625F, CubicTweenEquation.IN_OUT.compute(0.25F), 0F);
		Assert.assertEquals(0.5F, CubicTweenEquation.IN_OUT.compute(0.5F), 0F);
		Assert.assertEquals(0.9375F, CubicTweenEquation.IN_OUT.compute(0.75F), 0F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - CubicTweenEquation.IN.compute(1F - t), CubicTweenEquation.OUT.compute(t), 0.0001F);
			Assert.assertEquals(1F - CubicTweenEquation.IN_OUT.compute(1F - t), CubicTweenEquation.IN_OUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void keepsGrowing() {
		for (final CubicTweenEquation cubic : new CubicTweenEquation[] {CubicTweenEquation.IN, CubicTweenEquation.OUT, CubicTweenEquation.IN_OUT}) {
			for (int i = 1; i <= 20; i++) {
				Assert.assertTrue(cubic.compute(i / 20F) > cubic.compute((i - 1) / 20F));
			}
		}
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Cubic.IN", CubicTweenEquation.IN.toString());
		Assert.assertEquals("Cubic.OUT", CubicTweenEquation.OUT.toString());
		Assert.assertEquals("Cubic.IN_OUT", CubicTweenEquation.IN_OUT.toString());
	}

}