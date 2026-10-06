package dev.joid.lib.animation.tweenengine.equation;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class ElasticTest {

	@After
	public void restoreTheDefaults() {
		for (final Elastic elastic : new Elastic[] {Elastic.IN, Elastic.OUT, Elastic.INOUT}) {
			elastic.a = 0F;
			elastic.p = 0F;
			elastic.setA = false;
			elastic.setP = false;
		}
	}

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final Elastic elastic : new Elastic[] {Elastic.IN, Elastic.OUT, Elastic.INOUT}) {
			Assert.assertEquals(0F, elastic.compute(0F), 0F);
			Assert.assertEquals(1F, elastic.compute(1F), 0F);
		}
	}

	@Test
	public void oscillatesAroundItsEnds() {
		Assert.assertEquals(-0.015625F, Elastic.IN.compute(0.5F), 0.0001F);
		Assert.assertEquals(1.015625F, Elastic.OUT.compute(0.5F), 0.0001F);
	}

	@Test
	public void oscillatesAroundBothEndsInOut() {
		Assert.assertEquals(0.0119694F, Elastic.INOUT.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.5F, Elastic.INOUT.compute(0.5F), 0.0001F);
		Assert.assertEquals(0.9880306F, Elastic.INOUT.compute(0.75F), 0.0001F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - Elastic.IN.compute(1F - t), Elastic.OUT.compute(t), 0.0001F);
			Assert.assertEquals(1F - Elastic.INOUT.compute(1F - t), Elastic.INOUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void swingsWiderWithALargerAmplitude() {
		Assert.assertSame(Elastic.IN, Elastic.IN.a(2F));
		Assert.assertSame(Elastic.OUT, Elastic.OUT.a(2F));
		Assert.assertSame(Elastic.INOUT, Elastic.INOUT.a(2F).p(0.5F));
		Assert.assertEquals(-0.0625F, Elastic.IN.compute(0.5F), 0.0001F);
		Assert.assertEquals(0.96875F, Elastic.OUT.compute(0.5F), 0.0001F);
		Assert.assertEquals(0.015625F, Elastic.INOUT.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.984375F, Elastic.INOUT.compute(0.75F), 0.0001F);
	}

	@Test
	public void keepsTheDefaultSwingWithAnAmplitudeBelowOne() {
		Elastic.IN.a(0.5F);
		Elastic.OUT.a(0.5F);
		Elastic.INOUT.a(0.5F);
		Assert.assertEquals(-0.015625F, Elastic.IN.compute(0.5F), 0.0001F);
		Assert.assertEquals(1.015625F, Elastic.OUT.compute(0.5F), 0.0001F);
		Assert.assertEquals(0.0119694F, Elastic.INOUT.compute(0.25F), 0.0001F);
	}

	@Test
	public void swingsSlowerWithALongerPeriod() {
		Assert.assertSame(Elastic.IN, Elastic.IN.p(0.6F));
		Assert.assertSame(Elastic.OUT, Elastic.OUT.p(0.6F));
		Assert.assertSame(Elastic.INOUT, Elastic.INOUT.p(0.5F));
		Assert.assertEquals(0.015625F, Elastic.IN.compute(0.5F), 0.0001F);
		Assert.assertEquals(0.984375F, Elastic.OUT.compute(0.5F), 0.0001F);
		Assert.assertEquals(0.015625F, Elastic.INOUT.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.984375F, Elastic.INOUT.compute(0.75F), 0.0001F);
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Elastic.IN", Elastic.IN.toString());
		Assert.assertEquals("Elastic.OUT", Elastic.OUT.toString());
		Assert.assertEquals("Elastic.INOUT", Elastic.INOUT.toString());
	}

}