package dev.joid.lib.animation.tween;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.animation.tween.equation.BackTweenEquation;
import dev.joid.lib.animation.tween.equation.BounceTweenEquation;
import dev.joid.lib.animation.tween.equation.CircTweenEquation;
import dev.joid.lib.animation.tween.equation.CubicTweenEquation;
import dev.joid.lib.animation.tween.equation.ElasticTweenEquation;
import dev.joid.lib.animation.tween.equation.ExpoTweenEquation;
import dev.joid.lib.animation.tween.equation.LinearTweenEquation;
import dev.joid.lib.animation.tween.equation.QuadTweenEquation;
import dev.joid.lib.animation.tween.equation.QuartTweenEquation;
import dev.joid.lib.animation.tween.equation.QuintTweenEquation;
import dev.joid.lib.animation.tween.equation.SineTweenEquation;

public class TweenEquationsTest {

	@Test
	public void namesTheLinearEquation() {
		Assert.assertSame(LinearTweenEquation.INOUT, TweenEquations.LINEAR);
	}

	@Test
	public void namesEveryPowerEquation() {
		Assert.assertSame(QuadTweenEquation.IN, TweenEquations.QUAD_IN);
		Assert.assertSame(QuadTweenEquation.OUT, TweenEquations.QUAD_OUT);
		Assert.assertSame(QuadTweenEquation.INOUT, TweenEquations.QUAD_INOUT);
		Assert.assertSame(CubicTweenEquation.IN, TweenEquations.CUBIC_IN);
		Assert.assertSame(CubicTweenEquation.OUT, TweenEquations.CUBIC_OUT);
		Assert.assertSame(CubicTweenEquation.INOUT, TweenEquations.CUBIC_INOUT);
		Assert.assertSame(QuartTweenEquation.IN, TweenEquations.QUART_IN);
		Assert.assertSame(QuartTweenEquation.OUT, TweenEquations.QUART_OUT);
		Assert.assertSame(QuartTweenEquation.INOUT, TweenEquations.QUART_INOUT);
		Assert.assertSame(QuintTweenEquation.IN, TweenEquations.QUINT_IN);
		Assert.assertSame(QuintTweenEquation.OUT, TweenEquations.QUINT_OUT);
		Assert.assertSame(QuintTweenEquation.INOUT, TweenEquations.QUINT_INOUT);
	}

	@Test
	public void namesEveryCurvedEquation() {
		Assert.assertSame(CircTweenEquation.IN, TweenEquations.CIRC_IN);
		Assert.assertSame(CircTweenEquation.OUT, TweenEquations.CIRC_OUT);
		Assert.assertSame(CircTweenEquation.INOUT, TweenEquations.CIRC_INOUT);
		Assert.assertSame(SineTweenEquation.IN, TweenEquations.SINE_IN);
		Assert.assertSame(SineTweenEquation.OUT, TweenEquations.SINE_OUT);
		Assert.assertSame(SineTweenEquation.INOUT, TweenEquations.SINE_INOUT);
		Assert.assertSame(ExpoTweenEquation.IN, TweenEquations.EXPO_IN);
		Assert.assertSame(ExpoTweenEquation.OUT, TweenEquations.EXPO_OUT);
		Assert.assertSame(ExpoTweenEquation.INOUT, TweenEquations.EXPO_INOUT);
	}

	@Test
	public void namesEveryOvershootingEquation() {
		Assert.assertSame(BackTweenEquation.IN, TweenEquations.BACK_IN);
		Assert.assertSame(BackTweenEquation.OUT, TweenEquations.BACK_OUT);
		Assert.assertSame(BackTweenEquation.INOUT, TweenEquations.BACK_INOUT);
		Assert.assertSame(BounceTweenEquation.IN, TweenEquations.BOUNCE_IN);
		Assert.assertSame(BounceTweenEquation.OUT, TweenEquations.BOUNCE_OUT);
		Assert.assertSame(BounceTweenEquation.INOUT, TweenEquations.BOUNCE_INOUT);
		Assert.assertSame(ElasticTweenEquation.IN, TweenEquations.ELASTIC_IN);
		Assert.assertSame(ElasticTweenEquation.OUT, TweenEquations.ELASTIC_OUT);
		Assert.assertSame(ElasticTweenEquation.INOUT, TweenEquations.ELASTIC_INOUT);
	}

}