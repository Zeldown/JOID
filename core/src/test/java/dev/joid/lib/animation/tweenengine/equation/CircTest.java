package dev.joid.lib.animation.tweenengine.equation;

import org.junit.Assert;
import org.junit.Test;

public class CircTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final Circ circ : new Circ[] {Circ.OUT, Circ.INOUT}) {
			Assert.assertEquals(0F, circ.compute(0F), 0F);
			Assert.assertEquals(1F, circ.compute(1F), 0F);
		}
	}

	@Test
	public void followsAQuarterCircleOut() {
		Assert.assertEquals(0.6614378F, Circ.OUT.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.8660254F, Circ.OUT.compute(0.5F), 0.0001F);
	}

	@Test
	public void joinsTwoQuarterCirclesInOut() {
		Assert.assertEquals(0.0669873F, Circ.INOUT.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.5F, Circ.INOUT.compute(0.5F), 0F);
		Assert.assertEquals(0.9330127F, Circ.INOUT.compute(0.75F), 0.0001F);
	}

	@Test
	public void mirrorsItsHalvesInOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - Circ.INOUT.compute(1F - t), Circ.INOUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void keepsGrowing() {
		for (final Circ circ : new Circ[] {Circ.OUT, Circ.INOUT}) {
			for (int i = 1; i <= 20; i++) {
				Assert.assertTrue(circ.compute(i / 20F) > circ.compute((i - 1) / 20F));
			}
		}
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Circ.IN", Circ.IN.toString());
		Assert.assertEquals("Circ.OUT", Circ.OUT.toString());
		Assert.assertEquals("Circ.INOUT", Circ.INOUT.toString());
	}

	@Test
	public void startsAtZeroAndEndsAtOneIn() {
		Assert.assertEquals(0F, Circ.IN.compute(0F), 0.0001F);
		Assert.assertEquals(1F, Circ.IN.compute(1F), 0.0001F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - Circ.OUT.compute(1F - t), Circ.IN.compute(t), 0.0001F);
		}
	}

}