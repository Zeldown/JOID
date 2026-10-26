package dev.joid.lib.animation.tween.path;

import org.junit.Assert;
import org.junit.Test;

public class LinearTweenPathTest {

	@Test
	public void joinsTwoPoints() {
		Assert.assertEquals(3F, new LinearTweenPath().compute(0.25F, new float[] {2F, 6F}, 2), 0F);
	}

	@Test
	public void passesThroughEveryPoint() {
		final float[] points = {0F, 10F, 30F};
		Assert.assertEquals(0F, new LinearTweenPath().compute(0F, points, 3), 0F);
		Assert.assertEquals(10F, new LinearTweenPath().compute(0.5F, points, 3), 0F);
		Assert.assertEquals(30F, new LinearTweenPath().compute(1F, points, 3), 0F);
	}

	@Test
	public void followsAStraightLineInsideEachSegment() {
		final float[] points = {0F, 10F, 30F};
		Assert.assertEquals(5F, new LinearTweenPath().compute(0.25F, points, 3), 0F);
		Assert.assertEquals(20F, new LinearTweenPath().compute(0.75F, points, 3), 0F);
	}

	@Test
	public void extendsItsEndSegments() {
		final float[] points = {0F, 10F, 30F};
		Assert.assertEquals(-10F, new LinearTweenPath().compute(-0.5F, points, 3), 0F);
		Assert.assertEquals(50F, new LinearTweenPath().compute(1.5F, points, 3), 0F);
	}

	@Test
	public void ignoresThePointsPastTheCount() {
		Assert.assertEquals(10F, new LinearTweenPath().compute(1F, new float[] {0F, 10F, 99F}, 2), 0F);
	}

}