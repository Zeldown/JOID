package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class ElasticTweenEquationTest {

	@Test
	public void startsAtZeroAndEndsAtOne() {
		for (final ElasticTweenEquation elastic : new ElasticTweenEquation[] {ElasticTweenEquation.IN, ElasticTweenEquation.OUT, ElasticTweenEquation.IN_OUT}) {
			Assert.assertEquals(0F, elastic.compute(0F), 0F);
			Assert.assertEquals(1F, elastic.compute(1F), 0F);
		}
	}

	@Test
	public void oscillatesAroundItsEnds() {
		Assert.assertEquals(-0.015625F, ElasticTweenEquation.IN.compute(0.5F), 0.0001F);
		Assert.assertEquals(1.015625F, ElasticTweenEquation.OUT.compute(0.5F), 0.0001F);
	}

	@Test
	public void oscillatesAroundBothEndsInOut() {
		Assert.assertEquals(0.0119694F, ElasticTweenEquation.IN_OUT.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.5F, ElasticTweenEquation.IN_OUT.compute(0.5F), 0.0001F);
		Assert.assertEquals(0.9880306F, ElasticTweenEquation.IN_OUT.compute(0.75F), 0.0001F);
	}

	@Test
	public void mirrorsInAndOut() {
		for (int i = 0; i <= 20; i++) {
			final float t = i / 20F;
			Assert.assertEquals(1F - ElasticTweenEquation.IN.compute(1F - t), ElasticTweenEquation.OUT.compute(t), 0.0001F);
			Assert.assertEquals(1F - ElasticTweenEquation.IN_OUT.compute(1F - t), ElasticTweenEquation.IN_OUT.compute(t), 0.0001F);
		}
	}

	@Test
	public void swingsWiderWithALargerAmplitude() {
		final ElasticTweenEquation inOut = ElasticTweenEquation.IN_OUT.amplitude(2F).period(0.5F);
		Assert.assertEquals(-0.0625F, ElasticTweenEquation.IN.amplitude(2F).compute(0.5F), 0.0001F);
		Assert.assertEquals(0.96875F, ElasticTweenEquation.OUT.amplitude(2F).compute(0.5F), 0.0001F);
		Assert.assertEquals(0.015625F, inOut.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.984375F, inOut.compute(0.75F), 0.0001F);
	}

	@Test
	public void keepsTheDefaultSwingWithAnAmplitudeBelowOne() {
		Assert.assertEquals(-0.015625F, ElasticTweenEquation.IN.amplitude(0.5F).compute(0.5F), 0.0001F);
		Assert.assertEquals(1.015625F, ElasticTweenEquation.OUT.amplitude(0.5F).compute(0.5F), 0.0001F);
		Assert.assertEquals(0.0119694F, ElasticTweenEquation.IN_OUT.amplitude(0.5F).compute(0.25F), 0.0001F);
	}

	@Test
	public void swingsSlowerWithALongerPeriod() {
		final ElasticTweenEquation inOut = ElasticTweenEquation.IN_OUT.period(0.5F);
		Assert.assertEquals(0.015625F, ElasticTweenEquation.IN.period(0.6F).compute(0.5F), 0.0001F);
		Assert.assertEquals(0.984375F, ElasticTweenEquation.OUT.period(0.6F).compute(0.5F), 0.0001F);
		Assert.assertEquals(0.015625F, inOut.compute(0.25F), 0.0001F);
		Assert.assertEquals(0.984375F, inOut.compute(0.75F), 0.0001F);
	}

	@Test
	public void keepsItsPeriodWhenItsAmplitudeChanges() {
		Assert.assertEquals(ElasticTweenEquation.IN_OUT.amplitude(2F).period(0.5F).compute(0.25F), ElasticTweenEquation.IN_OUT.period(0.5F).amplitude(2F).compute(0.25F), 0F);
	}

	@Test
	public void leavesTheSharedEquationsUntouched() {
		Assert.assertNotSame(ElasticTweenEquation.IN, ElasticTweenEquation.IN.amplitude(2F));
		Assert.assertNotSame(ElasticTweenEquation.OUT, ElasticTweenEquation.OUT.period(0.6F));
		Assert.assertNotSame(ElasticTweenEquation.IN_OUT, ElasticTweenEquation.IN_OUT.amplitude(2F).period(0.5F));
		Assert.assertEquals(-0.015625F, ElasticTweenEquation.IN.compute(0.5F), 0.0001F);
		Assert.assertEquals(1.015625F, ElasticTweenEquation.OUT.compute(0.5F), 0.0001F);
		Assert.assertEquals(0.0119694F, ElasticTweenEquation.IN_OUT.compute(0.25F), 0.0001F);
	}

	@Test
	public void namesItsParametersWhenTheyAreTuned() {
		Assert.assertEquals("Elastic.IN(0.3, 2.0)", ElasticTweenEquation.IN.amplitude(2F).toString());
		Assert.assertEquals("Elastic.OUT(0.6, 0.0)", ElasticTweenEquation.OUT.period(0.6F).toString());
		Assert.assertEquals("Elastic.IN_OUT(0.5, 2.0)", ElasticTweenEquation.IN_OUT.amplitude(2F).period(0.5F).toString());
		Assert.assertEquals("Elastic.OUT", ElasticTweenEquation.OUT.period(0.3F).toString());
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Elastic.IN", ElasticTweenEquation.IN.toString());
		Assert.assertEquals("Elastic.OUT", ElasticTweenEquation.OUT.toString());
		Assert.assertEquals("Elastic.IN_OUT", ElasticTweenEquation.IN_OUT.toString());
	}

}