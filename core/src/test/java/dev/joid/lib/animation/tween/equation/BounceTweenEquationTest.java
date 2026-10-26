package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class BounceTweenEquationTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final BounceTweenEquation bounce : new BounceTweenEquation[] {BounceTweenEquation.IN, BounceTweenEquation.OUT, BounceTweenEquation.IN_OUT}) {
			Assert.assertEquals(0F, bounce.compute(0F), 0.0001F);
			Assert.assertEquals(1F, bounce.compute(1F), 0.0001F);
		}
	}

	@Test
	public void touchesTheTargetOnEveryBounceOut() {
		Assert.assertEquals(1F, BounceTweenEquation.OUT.compute(1F / 2.75F), 0.0001F);
		Assert.assertEquals(1F, BounceTweenEquation.OUT.compute(2F / 2.75F), 0.0001F);
		Assert.assertEquals(1F, BounceTweenEquation.OUT.compute(2.5F / 2.75F), 0.0001F);
	}

	@Test
	public void bouncesLowerEachTimeOut() {
		Assert.assertEquals(0.3025F, BounceTweenEquation.OUT.compute(0.2F), 0.0001F);
		Assert.assertEquals(0.7725F, BounceTweenEquation.OUT.compute(0.6F), 0.0001F);
		Assert.assertEquals(0.9451563F, BounceTweenEquation.OUT.compute(0.85F), 0.0001F);
		Assert.assertEquals(0.9845313F, BounceTweenEquation.OUT.compute(0.95F), 0.0001F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - BounceTweenEquation.IN.compute(1F - t), BounceTweenEquation.OUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void bouncesInThenOutInOut() {
		Assert.assertEquals(0.1171875F, BounceTweenEquation.IN_OUT.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.5F, BounceTweenEquation.IN_OUT.compute(0.5F), 0F);
		Assert.assertEquals(0.8828125F, BounceTweenEquation.IN_OUT.compute(0.75F), 0.0001F);
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - BounceTweenEquation.IN_OUT.compute(1F - t), BounceTweenEquation.IN_OUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Bounce.IN", BounceTweenEquation.IN.toString());
		Assert.assertEquals("Bounce.OUT", BounceTweenEquation.OUT.toString());
		Assert.assertEquals("Bounce.IN_OUT", BounceTweenEquation.IN_OUT.toString());
	}

}