package dev.joid.lib.animation.tweenengine.path;

import org.junit.Assert;
import org.junit.Test;

public class CatmullRomTest {

	@Test
	public void passesThroughEveryPoint() {
		final float[] points = {0F, 10F, 30F};
		Assert.assertEquals(0F, new CatmullRom().compute(0F, points, 3), 0F);
		Assert.assertEquals(10F, new CatmullRom().compute(0.5F, points, 3), 0F);
		Assert.assertEquals(30F, new CatmullRom().compute(1F, points, 3), 0F);
	}

	@Test
	public void curvesBetweenThePoints() {
		final float[] points = {0F, 10F, 30F};
		Assert.assertEquals(3.75F, new CatmullRom().compute(0.25F, points, 3), 0.0001F);
		Assert.assertEquals(20.625F, new CatmullRom().compute(0.75F, points, 3), 0.0001F);
	}

	@Test
	public void staysStraightBetweenEvenlySpacedPoints() {
		final float[] points = {0F, 10F, 20F, 30F};
		Assert.assertEquals(12F, new CatmullRom().compute(0.4F, points, 4), 0.0001F);
		Assert.assertEquals(15F, new CatmullRom().compute(0.5F, points, 4), 0.0001F);
		Assert.assertEquals(18F, new CatmullRom().compute(0.6F, points, 4), 0.0001F);
	}

	@Test
	public void joinsTwoPoints() {
		final float[] points = {0F, 10F};
		Assert.assertEquals(0F, new CatmullRom().compute(0F, points, 2), 0.0001F);
		Assert.assertEquals(5F, new CatmullRom().compute(0.5F, points, 2), 0.0001F);
		Assert.assertEquals(10F, new CatmullRom().compute(1F, points, 2), 0.0001F);
	}

}