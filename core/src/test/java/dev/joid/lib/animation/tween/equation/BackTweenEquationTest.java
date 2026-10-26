package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class BackTweenEquationTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final BackTweenEquation back : new BackTweenEquation[] {BackTweenEquation.IN, BackTweenEquation.OUT, BackTweenEquation.INOUT}) {
			Assert.assertEquals(0F, back.compute(0F), 0.0001F);
			Assert.assertEquals(1F, back.compute(1F), 0.0001F);
		}
	}

	@Test
	public void pullsBackBeforeLeavingIn() {
		Assert.assertEquals(-0.0876975F, BackTweenEquation.IN.compute(0.5F), 0.0001F);
	}

	@Test
	public void overshootsBeforeSettlingOut() {
		Assert.assertEquals(1.0876975F, BackTweenEquation.OUT.compute(0.5F), 0.0001F);
	}

	@Test
	public void pullsBackThenOvershootsInOut() {
		Assert.assertEquals(-0.0996818F, BackTweenEquation.INOUT.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.5F, BackTweenEquation.INOUT.compute(0.5F), 0.0001F);
		Assert.assertEquals(1.0996818F, BackTweenEquation.INOUT.compute(0.75F), 0.0001F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - BackTweenEquation.IN.compute(1F - t), BackTweenEquation.OUT.compute(t), 0.0001F);
			Assert.assertEquals(1F - BackTweenEquation.INOUT.compute(1F - t), BackTweenEquation.INOUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void becomesACubicWithoutOvershoot() {
		final BackTweenEquation in = BackTweenEquation.IN.overshoot(0F);
		final BackTweenEquation out = BackTweenEquation.OUT.overshoot(0F);
		final BackTweenEquation inOut = BackTweenEquation.INOUT.overshoot(0F);
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(CubicTweenEquation.IN.compute(t), in.compute(t), 0.0001F);
			Assert.assertEquals(CubicTweenEquation.OUT.compute(t), out.compute(t), 0.0001F);
			Assert.assertEquals(CubicTweenEquation.INOUT.compute(t), inOut.compute(t), 0.0001F);
		}
	}

	@Test
	public void leavesTheSharedEquationsUntouched() {
		Assert.assertNotSame(BackTweenEquation.IN, BackTweenEquation.IN.overshoot(0F));
		Assert.assertNotSame(BackTweenEquation.OUT, BackTweenEquation.OUT.overshoot(0F));
		Assert.assertNotSame(BackTweenEquation.INOUT, BackTweenEquation.INOUT.overshoot(0F));
		Assert.assertEquals(-0.0876975F, BackTweenEquation.IN.compute(0.5F), 0.0001F);
		Assert.assertEquals(1.0876975F, BackTweenEquation.OUT.compute(0.5F), 0.0001F);
		Assert.assertEquals(-0.0996818F, BackTweenEquation.INOUT.compute(0.25F), 0.0001F);
	}

	@Test
	public void keepsItsNameWithAnotherOvershoot() {
		Assert.assertEquals("Back.IN", BackTweenEquation.IN.overshoot(3F).toString());
		Assert.assertEquals("Back.OUT", BackTweenEquation.OUT.overshoot(3F).toString());
		Assert.assertEquals("Back.INOUT", BackTweenEquation.INOUT.overshoot(3F).toString());
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Back.IN", BackTweenEquation.IN.toString());
		Assert.assertEquals("Back.OUT", BackTweenEquation.OUT.toString());
		Assert.assertEquals("Back.INOUT", BackTweenEquation.INOUT.toString());
	}

}