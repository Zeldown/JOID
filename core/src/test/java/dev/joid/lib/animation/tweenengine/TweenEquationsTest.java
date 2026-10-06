package dev.joid.lib.animation.tweenengine;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.animation.tweenengine.equation.Back;
import dev.joid.lib.animation.tweenengine.equation.Bounce;
import dev.joid.lib.animation.tweenengine.equation.Circ;
import dev.joid.lib.animation.tweenengine.equation.Cubic;
import dev.joid.lib.animation.tweenengine.equation.Elastic;
import dev.joid.lib.animation.tweenengine.equation.Expo;
import dev.joid.lib.animation.tweenengine.equation.Linear;
import dev.joid.lib.animation.tweenengine.equation.Quad;
import dev.joid.lib.animation.tweenengine.equation.Quart;
import dev.joid.lib.animation.tweenengine.equation.Quint;
import dev.joid.lib.animation.tweenengine.equation.Sine;

public class TweenEquationsTest {

	@Test
	public void namesTheLinearEquation() {
		Assert.assertSame(Linear.INOUT, TweenEquations.LINEAR);
	}

	@Test
	public void namesEveryPowerEquation() {
		Assert.assertSame(Quad.IN, TweenEquations.QUAD_IN);
		Assert.assertSame(Quad.OUT, TweenEquations.QUAD_OUT);
		Assert.assertSame(Quad.INOUT, TweenEquations.QUAD_INOUT);
		Assert.assertSame(Cubic.IN, TweenEquations.CUBIC_IN);
		Assert.assertSame(Cubic.OUT, TweenEquations.CUBIC_OUT);
		Assert.assertSame(Cubic.INOUT, TweenEquations.CUBIC_INOUT);
		Assert.assertSame(Quart.IN, TweenEquations.QUART_IN);
		Assert.assertSame(Quart.OUT, TweenEquations.QUART_OUT);
		Assert.assertSame(Quart.INOUT, TweenEquations.QUART_INOUT);
		Assert.assertSame(Quint.IN, TweenEquations.QUINT_IN);
		Assert.assertSame(Quint.OUT, TweenEquations.QUINT_OUT);
		Assert.assertSame(Quint.INOUT, TweenEquations.QUINT_INOUT);
	}

	@Test
	public void namesEveryCurvedEquation() {
		Assert.assertSame(Circ.IN, TweenEquations.CIRC_IN);
		Assert.assertSame(Circ.OUT, TweenEquations.CIRC_OUT);
		Assert.assertSame(Circ.INOUT, TweenEquations.CIRC_INOUT);
		Assert.assertSame(Sine.IN, TweenEquations.SINE_IN);
		Assert.assertSame(Sine.OUT, TweenEquations.SINE_OUT);
		Assert.assertSame(Sine.INOUT, TweenEquations.SINE_INOUT);
		Assert.assertSame(Expo.IN, TweenEquations.EXPO_IN);
		Assert.assertSame(Expo.OUT, TweenEquations.EXPO_OUT);
		Assert.assertSame(Expo.INOUT, TweenEquations.EXPO_INOUT);
	}

	@Test
	public void namesEveryOvershootingEquation() {
		Assert.assertSame(Back.IN, TweenEquations.BACK_IN);
		Assert.assertSame(Back.OUT, TweenEquations.BACK_OUT);
		Assert.assertSame(Back.INOUT, TweenEquations.BACK_INOUT);
		Assert.assertSame(Bounce.IN, TweenEquations.BOUNCE_IN);
		Assert.assertSame(Bounce.OUT, TweenEquations.BOUNCE_OUT);
		Assert.assertSame(Bounce.INOUT, TweenEquations.BOUNCE_INOUT);
		Assert.assertSame(Elastic.IN, TweenEquations.ELASTIC_IN);
		Assert.assertSame(Elastic.OUT, TweenEquations.ELASTIC_OUT);
		Assert.assertSame(Elastic.INOUT, TweenEquations.ELASTIC_INOUT);
	}

}