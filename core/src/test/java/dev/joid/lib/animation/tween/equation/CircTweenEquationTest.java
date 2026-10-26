package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class CircTweenEquationTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final CircTweenEquation circ : new CircTweenEquation[] {CircTweenEquation.OUT, CircTweenEquation.IN_OUT}) {
			Assert.assertEquals(0F, circ.compute(0F), 0F);
			Assert.assertEquals(1F, circ.compute(1F), 0F);
		}
	}

	@Test
	public void followsAQuarterCircleOut() {
		Assert.assertEquals(0.6614378F, CircTweenEquation.OUT.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.8660254F, CircTweenEquation.OUT.compute(0.5F), 0.0001F);
	}

	@Test
	public void joinsTwoQuarterCirclesInOut() {
		Assert.assertEquals(0.0669873F, CircTweenEquation.IN_OUT.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.5F, CircTweenEquation.IN_OUT.compute(0.5F), 0F);
		Assert.assertEquals(0.9330127F, CircTweenEquation.IN_OUT.compute(0.75F), 0.0001F);
	}

	@Test
	public void mirrorsItsHalvesInOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - CircTweenEquation.IN_OUT.compute(1F - t), CircTweenEquation.IN_OUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void keepsGrowing() {
		for (final CircTweenEquation circ : new CircTweenEquation[] {CircTweenEquation.OUT, CircTweenEquation.IN_OUT}) {
			for (int i = 1; i <= 20; i++) {
				Assert.assertTrue(circ.compute(i / 20F) > circ.compute((i - 1) / 20F));
			}
		}
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Circ.IN", CircTweenEquation.IN.toString());
		Assert.assertEquals("Circ.OUT", CircTweenEquation.OUT.toString());
		Assert.assertEquals("Circ.IN_OUT", CircTweenEquation.IN_OUT.toString());
	}

	@Test
	public void startsAtZeroAndEndsAtOneIn() {
		Assert.assertEquals(0F, CircTweenEquation.IN.compute(0F), 0.0001F);
		Assert.assertEquals(1F, CircTweenEquation.IN.compute(1F), 0.0001F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - CircTweenEquation.OUT.compute(1F - t), CircTweenEquation.IN.compute(t), 0.0001F);
		}
	}

}