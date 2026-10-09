package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class BackTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final Back back : new Back[] {Back.IN, Back.OUT, Back.INOUT}) {
			Assert.assertEquals(0F, back.compute(0F), 0.0001F);
			Assert.assertEquals(1F, back.compute(1F), 0.0001F);
		}
	}

	@Test
	public void pullsBackBeforeLeavingIn() {
		Assert.assertEquals(-0.0876975F, Back.IN.compute(0.5F), 0.0001F);
	}

	@Test
	public void overshootsBeforeSettlingOut() {
		Assert.assertEquals(1.0876975F, Back.OUT.compute(0.5F), 0.0001F);
	}

	@Test
	public void pullsBackThenOvershootsInOut() {
		Assert.assertEquals(-0.0996818F, Back.INOUT.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.5F, Back.INOUT.compute(0.5F), 0.0001F);
		Assert.assertEquals(1.0996818F, Back.INOUT.compute(0.75F), 0.0001F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - Back.IN.compute(1F - t), Back.OUT.compute(t), 0.0001F);
			Assert.assertEquals(1F - Back.INOUT.compute(1F - t), Back.INOUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void becomesACubicWithoutOvershoot() {
		final Back in = Back.IN.s(0F);
		final Back out = Back.OUT.s(0F);
		final Back inOut = Back.INOUT.s(0F);
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(Cubic.IN.compute(t), in.compute(t), 0.0001F);
			Assert.assertEquals(Cubic.OUT.compute(t), out.compute(t), 0.0001F);
			Assert.assertEquals(Cubic.INOUT.compute(t), inOut.compute(t), 0.0001F);
		}
	}

	@Test
	public void leavesTheSharedEquationsUntouched() {
		Assert.assertNotSame(Back.IN, Back.IN.s(0F));
		Assert.assertNotSame(Back.OUT, Back.OUT.s(0F));
		Assert.assertNotSame(Back.INOUT, Back.INOUT.s(0F));
		Assert.assertEquals(-0.0876975F, Back.IN.compute(0.5F), 0.0001F);
		Assert.assertEquals(1.0876975F, Back.OUT.compute(0.5F), 0.0001F);
		Assert.assertEquals(-0.0996818F, Back.INOUT.compute(0.25F), 0.0001F);
	}

	@Test
	public void keepsItsNameWithAnotherOvershoot() {
		Assert.assertEquals("Back.IN", Back.IN.s(3F).toString());
		Assert.assertEquals("Back.OUT", Back.OUT.s(3F).toString());
		Assert.assertEquals("Back.INOUT", Back.INOUT.s(3F).toString());
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Back.IN", Back.IN.toString());
		Assert.assertEquals("Back.OUT", Back.OUT.toString());
		Assert.assertEquals("Back.INOUT", Back.INOUT.toString());
	}

}