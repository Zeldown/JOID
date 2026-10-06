package dev.joid.lib.animation.tweenengine.equation;

import org.junit.Assert;
import org.junit.Test;

public class ExpoTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final Expo expo : new Expo[] {Expo.IN, Expo.OUT, Expo.INOUT}) {
			Assert.assertEquals(0F, expo.compute(0F), 0F);
			Assert.assertEquals(1F, expo.compute(1F), 0F);
		}
	}

	@Test
	public void doublesEveryTenthIn() {
		Assert.assertEquals(0.03125F, Expo.IN.compute(0.5F), 0F);
		Assert.assertEquals(0.0625F, Expo.IN.compute(0.6F), 0.0001F);
	}

	@Test
	public void halvesTheRemainderEveryTenthOut() {
		Assert.assertEquals(0.96875F, Expo.OUT.compute(0.5F), 0F);
		Assert.assertEquals(0.984375F, Expo.OUT.compute(0.6F), 0.0001F);
	}

	@Test
	public void acceleratesThenDeceleratesInOut() {
		Assert.assertEquals(0.015625F, Expo.INOUT.compute(0.25F), 0F);
		Assert.assertEquals(0.5F, Expo.INOUT.compute(0.5F), 0F);
		Assert.assertEquals(0.984375F, Expo.INOUT.compute(0.75F), 0F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 1; i < 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - Expo.IN.compute(1F - t), Expo.OUT.compute(t), 0.0001F);
			Assert.assertEquals(1F - Expo.INOUT.compute(1F - t), Expo.INOUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void keepsGrowing() {
		for (final Expo expo : new Expo[] {Expo.IN, Expo.OUT, Expo.INOUT}) {
			for (int i = 1; i <= 20; i++) {
				Assert.assertTrue(expo.compute(i / 20F) > expo.compute((i - 1) / 20F));
			}
		}
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Expo.IN", Expo.IN.toString());
		Assert.assertEquals("Expo.OUT", Expo.OUT.toString());
		Assert.assertEquals("Expo.INOUT", Expo.INOUT.toString());
	}

}