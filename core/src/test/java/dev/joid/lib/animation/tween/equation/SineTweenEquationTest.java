package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class SineTweenEquationTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final SineTweenEquation sine : new SineTweenEquation[] {SineTweenEquation.IN, SineTweenEquation.OUT, SineTweenEquation.IN_OUT}) {
			Assert.assertEquals(0F, sine.compute(0F), 0F);
			Assert.assertEquals(1F, sine.compute(1F), 0.0001F);
		}
	}

	@Test
	public void acceleratesIn() {
		Assert.assertEquals(0.2928932F, SineTweenEquation.IN.compute(0.5F), 0.0001F);
	}

	@Test
	public void deceleratesOut() {
		Assert.assertEquals(0.7071068F, SineTweenEquation.OUT.compute(0.5F), 0.0001F);
	}

	@Test
	public void acceleratesThenDeceleratesInOut() {
		Assert.assertEquals(0.1464466F, SineTweenEquation.IN_OUT.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.5F, SineTweenEquation.IN_OUT.compute(0.5F), 0.0001F);
		Assert.assertEquals(0.8535534F, SineTweenEquation.IN_OUT.compute(0.75F), 0.0001F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - SineTweenEquation.IN.compute(1F - t), SineTweenEquation.OUT.compute(t), 0.0001F);
			Assert.assertEquals(1F - SineTweenEquation.IN_OUT.compute(1F - t), SineTweenEquation.IN_OUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void keepsGrowing() {
		for (final SineTweenEquation sine : new SineTweenEquation[] {SineTweenEquation.IN, SineTweenEquation.OUT, SineTweenEquation.IN_OUT}) {
			for (int i = 1; i <= 20; i++) {
				Assert.assertTrue(sine.compute(i / 20F) > sine.compute((i - 1) / 20F));
			}
		}
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Sine.IN", SineTweenEquation.IN.toString());
		Assert.assertEquals("Sine.OUT", SineTweenEquation.OUT.toString());
		Assert.assertEquals("Sine.IN_OUT", SineTweenEquation.IN_OUT.toString());
	}

}