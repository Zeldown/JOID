package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class QuintTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final Quint quint : new Quint[] {Quint.IN, Quint.OUT, Quint.INOUT}) {
			Assert.assertEquals(0F, quint.compute(0F), 0F);
			Assert.assertEquals(1F, quint.compute(1F), 0F);
		}
	}

	@Test
	public void acceleratesIn() {
		Assert.assertEquals(0.0009765625F, Quint.IN.compute(0.25F), 0F);
		Assert.assertEquals(0.03125F, Quint.IN.compute(0.5F), 0F);
	}

	@Test
	public void deceleratesOut() {
		Assert.assertEquals(0.7626953125F, Quint.OUT.compute(0.25F), 0F);
		Assert.assertEquals(0.96875F, Quint.OUT.compute(0.5F), 0F);
	}

	@Test
	public void acceleratesThenDeceleratesInOut() {
		Assert.assertEquals(0.015625F, Quint.INOUT.compute(0.25F), 0F);
		Assert.assertEquals(0.5F, Quint.INOUT.compute(0.5F), 0F);
		Assert.assertEquals(0.984375F, Quint.INOUT.compute(0.75F), 0F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - Quint.IN.compute(1F - t), Quint.OUT.compute(t), 0.0001F);
			Assert.assertEquals(1F - Quint.INOUT.compute(1F - t), Quint.INOUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void keepsGrowing() {
		for (final Quint quint : new Quint[] {Quint.IN, Quint.OUT, Quint.INOUT}) {
			for (int i = 1; i <= 20; i++) {
				Assert.assertTrue(quint.compute(i / 20F) > quint.compute((i - 1) / 20F));
			}
		}
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Quint.IN", Quint.IN.toString());
		Assert.assertEquals("Quint.OUT", Quint.OUT.toString());
		Assert.assertEquals("Quint.INOUT", Quint.INOUT.toString());
	}

}