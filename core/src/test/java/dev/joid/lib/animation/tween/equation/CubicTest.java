package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class CubicTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final Cubic cubic : new Cubic[] {Cubic.IN, Cubic.OUT, Cubic.INOUT}) {
			Assert.assertEquals(0F, cubic.compute(0F), 0F);
			Assert.assertEquals(1F, cubic.compute(1F), 0F);
		}
	}

	@Test
	public void acceleratesIn() {
		Assert.assertEquals(0.015625F, Cubic.IN.compute(0.25F), 0F);
		Assert.assertEquals(0.125F, Cubic.IN.compute(0.5F), 0F);
	}

	@Test
	public void deceleratesOut() {
		Assert.assertEquals(0.578125F, Cubic.OUT.compute(0.25F), 0F);
		Assert.assertEquals(0.875F, Cubic.OUT.compute(0.5F), 0F);
	}

	@Test
	public void acceleratesThenDeceleratesInOut() {
		Assert.assertEquals(0.0625F, Cubic.INOUT.compute(0.25F), 0F);
		Assert.assertEquals(0.5F, Cubic.INOUT.compute(0.5F), 0F);
		Assert.assertEquals(0.9375F, Cubic.INOUT.compute(0.75F), 0F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - Cubic.IN.compute(1F - t), Cubic.OUT.compute(t), 0.0001F);
			Assert.assertEquals(1F - Cubic.INOUT.compute(1F - t), Cubic.INOUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void keepsGrowing() {
		for (final Cubic cubic : new Cubic[] {Cubic.IN, Cubic.OUT, Cubic.INOUT}) {
			for (int i = 1; i <= 20; i++) {
				Assert.assertTrue(cubic.compute(i / 20F) > cubic.compute((i - 1) / 20F));
			}
		}
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Cubic.IN", Cubic.IN.toString());
		Assert.assertEquals("Cubic.OUT", Cubic.OUT.toString());
		Assert.assertEquals("Cubic.INOUT", Cubic.INOUT.toString());
	}

}