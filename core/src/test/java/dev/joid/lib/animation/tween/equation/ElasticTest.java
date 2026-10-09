package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class ElasticTest {

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
		final Elastic inOut = Elastic.INOUT.a(2F).p(0.5F);
		Assert.assertEquals(-0.0625F, Elastic.IN.a(2F).compute(0.5F), 0.0001F);
		Assert.assertEquals(0.96875F, Elastic.OUT.a(2F).compute(0.5F), 0.0001F);
		Assert.assertEquals(0.015625F, inOut.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.984375F, inOut.compute(0.75F), 0.0001F);
	}

	@Test
	public void keepsTheDefaultSwingWithAnAmplitudeBelowOne() {
		Assert.assertEquals(-0.015625F, Elastic.IN.a(0.5F).compute(0.5F), 0.0001F);
		Assert.assertEquals(1.015625F, Elastic.OUT.a(0.5F).compute(0.5F), 0.0001F);
		Assert.assertEquals(0.0119694F, Elastic.INOUT.a(0.5F).compute(0.25F), 0.0001F);
	}

	@Test
	public void swingsSlowerWithALongerPeriod() {
		final Elastic inOut = Elastic.INOUT.p(0.5F);
		Assert.assertEquals(0.015625F, Elastic.IN.p(0.6F).compute(0.5F), 0.0001F);
		Assert.assertEquals(0.984375F, Elastic.OUT.p(0.6F).compute(0.5F), 0.0001F);
		Assert.assertEquals(0.015625F, inOut.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.984375F, inOut.compute(0.75F), 0.0001F);
	}

	@Test
	public void keepsItsPeriodWhenItsAmplitudeChanges() {
		Assert.assertEquals(Elastic.INOUT.a(2F).p(0.5F).compute(0.25F), Elastic.INOUT.p(0.5F).a(2F).compute(0.25F), 0F);
	}

	@Test
	public void leavesTheSharedEquationsUntouched() {
		Assert.assertNotSame(Elastic.IN, Elastic.IN.a(2F));
		Assert.assertNotSame(Elastic.OUT, Elastic.OUT.p(0.6F));
		Assert.assertNotSame(Elastic.INOUT, Elastic.INOUT.a(2F).p(0.5F));
		Assert.assertEquals(-0.015625F, Elastic.IN.compute(0.5F), 0.0001F);
		Assert.assertEquals(1.015625F, Elastic.OUT.compute(0.5F), 0.0001F);
		Assert.assertEquals(0.0119694F, Elastic.INOUT.compute(0.25F), 0.0001F);
	}

	@Test
	public void keepsItsNameWithOtherParameters() {
		Assert.assertEquals("Elastic.IN", Elastic.IN.a(2F).toString());
		Assert.assertEquals("Elastic.OUT", Elastic.OUT.p(0.6F).toString());
		Assert.assertEquals("Elastic.INOUT", Elastic.INOUT.a(2F).p(0.5F).toString());
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Elastic.IN", Elastic.IN.toString());
		Assert.assertEquals("Elastic.OUT", Elastic.OUT.toString());
		Assert.assertEquals("Elastic.INOUT", Elastic.INOUT.toString());
	}

}