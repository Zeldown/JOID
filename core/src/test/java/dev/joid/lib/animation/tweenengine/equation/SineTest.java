package dev.joid.lib.animation.tweenengine.equation;

import org.junit.Assert;
import org.junit.Test;

public class SineTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final Sine sine : new Sine[] {Sine.IN, Sine.OUT, Sine.INOUT}) {
			Assert.assertEquals(0F, sine.compute(0F), 0F);
			Assert.assertEquals(1F, sine.compute(1F), 0.0001F);
		}
	}

	@Test
	public void acceleratesIn() {
		Assert.assertEquals(0.2928932F, Sine.IN.compute(0.5F), 0.0001F);
	}

	@Test
	public void deceleratesOut() {
		Assert.assertEquals(0.7071068F, Sine.OUT.compute(0.5F), 0.0001F);
	}

	@Test
	public void acceleratesThenDeceleratesInOut() {
		Assert.assertEquals(0.1464466F, Sine.INOUT.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.5F, Sine.INOUT.compute(0.5F), 0.0001F);
		Assert.assertEquals(0.8535534F, Sine.INOUT.compute(0.75F), 0.0001F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - Sine.IN.compute(1F - t), Sine.OUT.compute(t), 0.0001F);
			Assert.assertEquals(1F - Sine.INOUT.compute(1F - t), Sine.INOUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void keepsGrowing() {
		for (final Sine sine : new Sine[] {Sine.IN, Sine.OUT, Sine.INOUT}) {
			for (int i = 1; i <= 20; i++) {
				Assert.assertTrue(sine.compute(i / 20F) > sine.compute((i - 1) / 20F));
			}
		}
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Sine.IN", Sine.IN.toString());
		Assert.assertEquals("Sine.OUT", Sine.OUT.toString());
		Assert.assertEquals("Sine.INOUT", Sine.INOUT.toString());
	}

}