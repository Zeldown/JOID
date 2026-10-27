package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class BackTweenEquationTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final BackTweenEquation back : new BackTweenEquation[] {BackTweenEquation.IN, BackTweenEquation.OUT, BackTweenEquation.IN_OUT}) {
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
		Assert.assertEquals(-0.0996818F, BackTweenEquation.IN_OUT.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.5F, BackTweenEquation.IN_OUT.compute(0.5F), 0.0001F);
		Assert.assertEquals(1.0996818F, BackTweenEquation.IN_OUT.compute(0.75F), 0.0001F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - BackTweenEquation.IN.compute(1F - t), BackTweenEquation.OUT.compute(t), 0.0001F);
			Assert.assertEquals(1F - BackTweenEquation.IN_OUT.compute(1F - t), BackTweenEquation.IN_OUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void becomesACubicWithoutOvershoot() {
		final BackTweenEquation in = BackTweenEquation.IN.overshoot(0F);
		final BackTweenEquation out = BackTweenEquation.OUT.overshoot(0F);
		final BackTweenEquation inOut = BackTweenEquation.IN_OUT.overshoot(0F);
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(CubicTweenEquation.IN.compute(t), in.compute(t), 0.0001F);
			Assert.assertEquals(CubicTweenEquation.OUT.compute(t), out.compute(t), 0.0001F);
			Assert.assertEquals(CubicTweenEquation.IN_OUT.compute(t), inOut.compute(t), 0.0001F);
		}
	}

	@Test
	public void leavesTheSharedEquationsUntouched() {
		Assert.assertNotSame(BackTweenEquation.IN, BackTweenEquation.IN.overshoot(0F));
		Assert.assertNotSame(BackTweenEquation.OUT, BackTweenEquation.OUT.overshoot(0F));
		Assert.assertNotSame(BackTweenEquation.IN_OUT, BackTweenEquation.IN_OUT.overshoot(0F));
		Assert.assertEquals(-0.0876975F, BackTweenEquation.IN.compute(0.5F), 0.0001F);
		Assert.assertEquals(1.0876975F, BackTweenEquation.OUT.compute(0.5F), 0.0001F);
		Assert.assertEquals(-0.0996818F, BackTweenEquation.IN_OUT.compute(0.25F), 0.0001F);
	}

	@Test
	public void namesItsOvershootWhenItIsTuned() {
		Assert.assertEquals("Back.IN(3.0)", BackTweenEquation.IN.overshoot(3F).toString());
		Assert.assertEquals("Back.OUT(2.5)", BackTweenEquation.OUT.overshoot(2.5F).toString());
		Assert.assertEquals("Back.IN_OUT(3.0)", BackTweenEquation.IN_OUT.overshoot(3F).toString());
		Assert.assertEquals("Back.IN", BackTweenEquation.IN.overshoot(BackTweenEquation.IN.getOvershoot()).toString());
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Back.IN", BackTweenEquation.IN.toString());
		Assert.assertEquals("Back.OUT", BackTweenEquation.OUT.toString());
		Assert.assertEquals("Back.IN_OUT", BackTweenEquation.IN_OUT.toString());
	}

}