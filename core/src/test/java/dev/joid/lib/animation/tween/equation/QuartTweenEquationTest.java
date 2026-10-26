package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class QuartTweenEquationTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final QuartTweenEquation quart : new QuartTweenEquation[] {QuartTweenEquation.IN, QuartTweenEquation.OUT, QuartTweenEquation.IN_OUT}) {
			Assert.assertEquals(0F, quart.compute(0F), 0F);
			Assert.assertEquals(1F, quart.compute(1F), 0F);
		}
	}

	@Test
	public void acceleratesIn() {
		Assert.assertEquals(0.00390625F, QuartTweenEquation.IN.compute(0.25F), 0F);
		Assert.assertEquals(0.0625F, QuartTweenEquation.IN.compute(0.5F), 0F);
	}

	@Test
	public void deceleratesOut() {
		Assert.assertEquals(0.68359375F, QuartTweenEquation.OUT.compute(0.25F), 0F);
		Assert.assertEquals(0.9375F, QuartTweenEquation.OUT.compute(0.5F), 0F);
	}

	@Test
	public void acceleratesThenDeceleratesInOut() {
		Assert.assertEquals(0.03125F, QuartTweenEquation.IN_OUT.compute(0.25F), 0F);
		Assert.assertEquals(0.5F, QuartTweenEquation.IN_OUT.compute(0.5F), 0F);
		Assert.assertEquals(0.96875F, QuartTweenEquation.IN_OUT.compute(0.75F), 0F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - QuartTweenEquation.IN.compute(1F - t), QuartTweenEquation.OUT.compute(t), 0.0001F);
			Assert.assertEquals(1F - QuartTweenEquation.IN_OUT.compute(1F - t), QuartTweenEquation.IN_OUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void keepsGrowing() {
		for (final QuartTweenEquation quart : new QuartTweenEquation[] {QuartTweenEquation.IN, QuartTweenEquation.OUT, QuartTweenEquation.IN_OUT}) {
			for (int i = 1; i <= 20; i++) {
				Assert.assertTrue(quart.compute(i / 20F) > quart.compute((i - 1) / 20F));
			}
		}
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Quart.IN", QuartTweenEquation.IN.toString());
		Assert.assertEquals("Quart.OUT", QuartTweenEquation.OUT.toString());
		Assert.assertEquals("Quart.IN_OUT", QuartTweenEquation.IN_OUT.toString());
	}

}