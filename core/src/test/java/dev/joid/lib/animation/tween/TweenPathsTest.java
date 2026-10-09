package dev.joid.lib.animation.tween;

import org.junit.Assert;
import org.junit.Test;

public class TweenPathsTest {

	@Test
	public void joinsThePointsWithStraightLines() {
		Assert.assertEquals(5F, TweenPaths.linear.compute(0.25F, new float[] {0F, 10F, 30F}, 3), 0F);
	}

	@Test
	public void joinsThePointsWithACurve() {
		Assert.assertEquals(3.75F, TweenPaths.catmullRom.compute(0.25F, new float[] {0F, 10F, 30F}, 3), 0.0001F);
	}

}