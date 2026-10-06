package dev.joid.lib.animation.tweenengine;

import org.junit.Assert;
import org.junit.Test;

public class TweenUtilsTest {

	@Test
	public void parsesEveryEquationByName() {
		for (final TweenEquation equation : new TweenEquation[] {TweenEquations.LINEAR, TweenEquations.QUAD_IN, TweenEquations.QUAD_OUT, TweenEquations.QUAD_INOUT, TweenEquations.CUBIC_IN, TweenEquations.CUBIC_OUT, TweenEquations.CUBIC_INOUT, TweenEquations.QUART_IN, TweenEquations.QUART_OUT, TweenEquations.QUART_INOUT, TweenEquations.QUINT_IN, TweenEquations.QUINT_OUT, TweenEquations.QUINT_INOUT, TweenEquations.CIRC_IN, TweenEquations.CIRC_OUT, TweenEquations.CIRC_INOUT, TweenEquations.SINE_IN, TweenEquations.SINE_OUT, TweenEquations.SINE_INOUT, TweenEquations.EXPO_IN, TweenEquations.EXPO_OUT, TweenEquations.EXPO_INOUT, TweenEquations.BACK_IN, TweenEquations.BACK_OUT, TweenEquations.BACK_INOUT, TweenEquations.BOUNCE_IN, TweenEquations.BOUNCE_OUT, TweenEquations.BOUNCE_INOUT, TweenEquations.ELASTIC_IN, TweenEquations.ELASTIC_OUT, TweenEquations.ELASTIC_INOUT}) {
			Assert.assertSame(equation, TweenUtils.parseEasing(equation.toString()).get());
		}
	}

	@Test
	public void parsesNothingFromAnUnknownName() {
		Assert.assertFalse(TweenUtils.parseEasing("Quad.SIDEWAYS").isPresent());
		Assert.assertFalse(TweenUtils.parseEasing("quad.in").isPresent());
	}

}