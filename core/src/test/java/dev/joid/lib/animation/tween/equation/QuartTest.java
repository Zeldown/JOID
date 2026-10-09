package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class QuartTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final Quart quart : new Quart[] {Quart.IN, Quart.OUT, Quart.INOUT}) {
			Assert.assertEquals(0F, quart.compute(0F), 0F);
			Assert.assertEquals(1F, quart.compute(1F), 0F);
		}
	}

	@Test
	public void acceleratesIn() {
		Assert.assertEquals(0.00390625F, Quart.IN.compute(0.25F), 0F);
		Assert.assertEquals(0.0625F, Quart.IN.compute(0.5F), 0F);
	}

	@Test
	public void deceleratesOut() {
		Assert.assertEquals(0.68359375F, Quart.OUT.compute(0.25F), 0F);
		Assert.assertEquals(0.9375F, Quart.OUT.compute(0.5F), 0F);
	}

	@Test
	public void acceleratesThenDeceleratesInOut() {
		Assert.assertEquals(0.03125F, Quart.INOUT.compute(0.25F), 0F);
		Assert.assertEquals(0.5F, Quart.INOUT.compute(0.5F), 0F);
		Assert.assertEquals(0.96875F, Quart.INOUT.compute(0.75F), 0F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - Quart.IN.compute(1F - t), Quart.OUT.compute(t), 0.0001F);
			Assert.assertEquals(1F - Quart.INOUT.compute(1F - t), Quart.INOUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void keepsGrowing() {
		for (final Quart quart : new Quart[] {Quart.IN, Quart.OUT, Quart.INOUT}) {
			for (int i = 1; i <= 20; i++) {
				Assert.assertTrue(quart.compute(i / 20F) > quart.compute((i - 1) / 20F));
			}
		}
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Quart.IN", Quart.IN.toString());
		Assert.assertEquals("Quart.OUT", Quart.OUT.toString());
		Assert.assertEquals("Quart.INOUT", Quart.INOUT.toString());
	}

}