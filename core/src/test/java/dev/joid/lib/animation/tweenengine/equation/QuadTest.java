package dev.joid.lib.animation.tweenengine.equation;

import org.junit.Assert;
import org.junit.Test;

public class QuadTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final Quad quad : new Quad[] {Quad.IN, Quad.OUT, Quad.INOUT}) {
			Assert.assertEquals(0F, quad.compute(0F), 0F);
			Assert.assertEquals(1F, quad.compute(1F), 0F);
		}
	}

	@Test
	public void acceleratesIn() {
		Assert.assertEquals(0.0625F, Quad.IN.compute(0.25F), 0F);
		Assert.assertEquals(0.25F, Quad.IN.compute(0.5F), 0F);
	}

	@Test
	public void deceleratesOut() {
		Assert.assertEquals(0.4375F, Quad.OUT.compute(0.25F), 0F);
		Assert.assertEquals(0.75F, Quad.OUT.compute(0.5F), 0F);
	}

	@Test
	public void acceleratesThenDeceleratesInOut() {
		Assert.assertEquals(0.125F, Quad.INOUT.compute(0.25F), 0F);
		Assert.assertEquals(0.5F, Quad.INOUT.compute(0.5F), 0F);
		Assert.assertEquals(0.875F, Quad.INOUT.compute(0.75F), 0F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - Quad.IN.compute(1F - t), Quad.OUT.compute(t), 0.0001F);
			Assert.assertEquals(1F - Quad.INOUT.compute(1F - t), Quad.INOUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void keepsGrowing() {
		for (final Quad quad : new Quad[] {Quad.IN, Quad.OUT, Quad.INOUT}) {
			for (int i = 1; i <= 20; i++) {
				Assert.assertTrue(quad.compute(i / 20F) > quad.compute((i - 1) / 20F));
			}
		}
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Quad.IN", Quad.IN.toString());
		Assert.assertEquals("Quad.OUT", Quad.OUT.toString());
		Assert.assertEquals("Quad.INOUT", Quad.INOUT.toString());
	}

}