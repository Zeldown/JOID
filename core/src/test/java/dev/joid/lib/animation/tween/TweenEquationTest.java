package dev.joid.lib.animation.tween;

import org.junit.Assert;
import org.junit.Test;

public class TweenEquationTest {

	@Test
	public void recognizesItsOwnName() {
		Assert.assertTrue(TweenEquations.QUAD_IN.isValueOf("Quad.IN"));
		Assert.assertFalse(TweenEquations.QUAD_IN.isValueOf("Quad.OUT"));
		Assert.assertFalse(TweenEquations.QUAD_IN.isValueOf("quad.in"));
	}

}