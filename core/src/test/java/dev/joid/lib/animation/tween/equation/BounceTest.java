package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class BounceTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final Bounce bounce : new Bounce[] {Bounce.IN, Bounce.OUT, Bounce.INOUT}) {
			Assert.assertEquals(0F, bounce.compute(0F), 0.0001F);
			Assert.assertEquals(1F, bounce.compute(1F), 0.0001F);
		}
	}

	@Test
	public void touchesTheTargetOnEveryBounceOut() {
		Assert.assertEquals(1F, Bounce.OUT.compute(1F / 2.75F), 0.0001F);
		Assert.assertEquals(1F, Bounce.OUT.compute(2F / 2.75F), 0.0001F);
		Assert.assertEquals(1F, Bounce.OUT.compute(2.5F / 2.75F), 0.0001F);
	}

	@Test
	public void bouncesLowerEachTimeOut() {
		Assert.assertEquals(0.3025F, Bounce.OUT.compute(0.2F), 0.0001F);
		Assert.assertEquals(0.7725F, Bounce.OUT.compute(0.6F), 0.0001F);
		Assert.assertEquals(0.9451563F, Bounce.OUT.compute(0.85F), 0.0001F);
		Assert.assertEquals(0.9845313F, Bounce.OUT.compute(0.95F), 0.0001F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - Bounce.IN.compute(1F - t), Bounce.OUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void bouncesInThenOutInOut() {
		Assert.assertEquals(0.1171875F, Bounce.INOUT.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.5F, Bounce.INOUT.compute(0.5F), 0F);
		Assert.assertEquals(0.8828125F, Bounce.INOUT.compute(0.75F), 0.0001F);
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - Bounce.INOUT.compute(1F - t), Bounce.INOUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Bounce.IN", Bounce.IN.toString());
		Assert.assertEquals("Bounce.OUT", Bounce.OUT.toString());
		Assert.assertEquals("Bounce.INOUT", Bounce.INOUT.toString());
	}

}