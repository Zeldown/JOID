package dev.joid.lib.animation.tween.equation;

import org.junit.Assert;
import org.junit.Test;

public class LinearTweenEquationTest {

	@Test
	public void returnsTheTimeUnchanged() {
		for (final float t : new float[] {-0.5F, 0F, 0.25F, 0.5F, 1F, 1.5F}) {
			Assert.assertEquals(t, LinearTweenEquation.IN_OUT.compute(t), 0F);
		}
	}

	@Test
	public void namesItself() {
		Assert.assertEquals("Linear.IN_OUT", LinearTweenEquation.IN_OUT.toString());
	}

}