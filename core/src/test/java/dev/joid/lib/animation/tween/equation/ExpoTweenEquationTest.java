package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class ExpoTweenEquationTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final ExpoTweenEquation expo : new ExpoTweenEquation[] {ExpoTweenEquation.IN, ExpoTweenEquation.OUT, ExpoTweenEquation.INOUT}) {
			Assert.assertEquals(0F, expo.compute(0F), 0F);
			Assert.assertEquals(1F, expo.compute(1F), 0F);
		}
	}

	@Test
	public void doublesEveryTenthIn() {
		Assert.assertEquals(0.03125F, ExpoTweenEquation.IN.compute(0.5F), 0F);
		Assert.assertEquals(0.0625F, ExpoTweenEquation.IN.compute(0.6F), 0.0001F);
	}

	@Test
	public void halvesTheRemainderEveryTenthOut() {
		Assert.assertEquals(0.96875F, ExpoTweenEquation.OUT.compute(0.5F), 0F);
		Assert.assertEquals(0.984375F, ExpoTweenEquation.OUT.compute(0.6F), 0.0001F);
	}

	@Test
	public void acceleratesThenDeceleratesInOut() {
		Assert.assertEquals(0.015625F, ExpoTweenEquation.INOUT.compute(0.25F), 0F);
		Assert.assertEquals(0.5F, ExpoTweenEquation.INOUT.compute(0.5F), 0F);
		Assert.assertEquals(0.984375F, ExpoTweenEquation.INOUT.compute(0.75F), 0F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 1; i < 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - ExpoTweenEquation.IN.compute(1F - t), ExpoTweenEquation.OUT.compute(t), 0.0001F);
			Assert.assertEquals(1F - ExpoTweenEquation.INOUT.compute(1F - t), ExpoTweenEquation.INOUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void keepsGrowing() {
		for (final ExpoTweenEquation expo : new ExpoTweenEquation[] {ExpoTweenEquation.IN, ExpoTweenEquation.OUT, ExpoTweenEquation.INOUT}) {
			for (int i = 1; i <= 20; i++) {
				Assert.assertTrue(expo.compute(i / 20F) > expo.compute((i - 1) / 20F));
			}
		}
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Expo.IN", ExpoTweenEquation.IN.toString());
		Assert.assertEquals("Expo.OUT", ExpoTweenEquation.OUT.toString());
		Assert.assertEquals("Expo.INOUT", ExpoTweenEquation.INOUT.toString());
	}

}