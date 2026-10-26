package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class QuintTweenEquationTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final QuintTweenEquation quint : new QuintTweenEquation[] {QuintTweenEquation.IN, QuintTweenEquation.OUT, QuintTweenEquation.IN_OUT}) {
			Assert.assertEquals(0F, quint.compute(0F), 0F);
			Assert.assertEquals(1F, quint.compute(1F), 0F);
		}
	}

	@Test
	public void acceleratesIn() {
		Assert.assertEquals(0.0009765625F, QuintTweenEquation.IN.compute(0.25F), 0F);
		Assert.assertEquals(0.03125F, QuintTweenEquation.IN.compute(0.5F), 0F);
	}

	@Test
	public void deceleratesOut() {
		Assert.assertEquals(0.7626953125F, QuintTweenEquation.OUT.compute(0.25F), 0F);
		Assert.assertEquals(0.96875F, QuintTweenEquation.OUT.compute(0.5F), 0F);
	}

	@Test
	public void acceleratesThenDeceleratesInOut() {
		Assert.assertEquals(0.015625F, QuintTweenEquation.IN_OUT.compute(0.25F), 0F);
		Assert.assertEquals(0.5F, QuintTweenEquation.IN_OUT.compute(0.5F), 0F);
		Assert.assertEquals(0.984375F, QuintTweenEquation.IN_OUT.compute(0.75F), 0F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - QuintTweenEquation.IN.compute(1F - t), QuintTweenEquation.OUT.compute(t), 0.0001F);
			Assert.assertEquals(1F - QuintTweenEquation.IN_OUT.compute(1F - t), QuintTweenEquation.IN_OUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void keepsGrowing() {
		for (final QuintTweenEquation quint : new QuintTweenEquation[] {QuintTweenEquation.IN, QuintTweenEquation.OUT, QuintTweenEquation.IN_OUT}) {
			for (int i = 1; i <= 20; i++) {
				Assert.assertTrue(quint.compute(i / 20F) > quint.compute((i - 1) / 20F));
			}
		}
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Quint.IN", QuintTweenEquation.IN.toString());
		Assert.assertEquals("Quint.OUT", QuintTweenEquation.OUT.toString());
		Assert.assertEquals("Quint.IN_OUT", QuintTweenEquation.IN_OUT.toString());
	}

}