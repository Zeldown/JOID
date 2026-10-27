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
		Assert.assertSame(LinearTweenEquation.IN_OUT, TweenEquations.LINEAR);
	}

	@Test
	public void namesEveryPowerEquation() {
		Assert.assertSame(QuadTweenEquation.IN, TweenEquations.QUAD_IN);
		Assert.assertSame(QuadTweenEquation.OUT, TweenEquations.QUAD_OUT);
		Assert.assertSame(QuadTweenEquation.IN_OUT, TweenEquations.QUAD_IN_OUT);
		Assert.assertSame(CubicTweenEquation.IN, TweenEquations.CUBIC_IN);
		Assert.assertSame(CubicTweenEquation.OUT, TweenEquations.CUBIC_OUT);
		Assert.assertSame(CubicTweenEquation.IN_OUT, TweenEquations.CUBIC_IN_OUT);
		Assert.assertSame(QuartTweenEquation.IN, TweenEquations.QUART_IN);
		Assert.assertSame(QuartTweenEquation.OUT, TweenEquations.QUART_OUT);
		Assert.assertSame(QuartTweenEquation.IN_OUT, TweenEquations.QUART_IN_OUT);
		Assert.assertSame(QuintTweenEquation.IN, TweenEquations.QUINT_IN);
		Assert.assertSame(QuintTweenEquation.OUT, TweenEquations.QUINT_OUT);
		Assert.assertSame(QuintTweenEquation.IN_OUT, TweenEquations.QUINT_IN_OUT);
	}

	@Test
	public void namesEveryCurvedEquation() {
		Assert.assertSame(CircTweenEquation.IN, TweenEquations.CIRC_IN);
		Assert.assertSame(CircTweenEquation.OUT, TweenEquations.CIRC_OUT);
		Assert.assertSame(CircTweenEquation.IN_OUT, TweenEquations.CIRC_IN_OUT);
		Assert.assertSame(SineTweenEquation.IN, TweenEquations.SINE_IN);
		Assert.assertSame(SineTweenEquation.OUT, TweenEquations.SINE_OUT);
		Assert.assertSame(SineTweenEquation.IN_OUT, TweenEquations.SINE_IN_OUT);
		Assert.assertSame(ExpoTweenEquation.IN, TweenEquations.EXPO_IN);
		Assert.assertSame(ExpoTweenEquation.OUT, TweenEquations.EXPO_OUT);
		Assert.assertSame(ExpoTweenEquation.IN_OUT, TweenEquations.EXPO_IN_OUT);
	}

	@Test
	public void namesEveryOvershootingEquation() {
		Assert.assertSame(BackTweenEquation.IN, TweenEquations.BACK_IN);
		Assert.assertSame(BackTweenEquation.OUT, TweenEquations.BACK_OUT);
		Assert.assertSame(BackTweenEquation.IN_OUT, TweenEquations.BACK_IN_OUT);
		Assert.assertSame(BounceTweenEquation.IN, TweenEquations.BOUNCE_IN);
		Assert.assertSame(BounceTweenEquation.OUT, TweenEquations.BOUNCE_OUT);
		Assert.assertSame(BounceTweenEquation.IN_OUT, TweenEquations.BOUNCE_IN_OUT);
		Assert.assertSame(ElasticTweenEquation.IN, TweenEquations.ELASTIC_IN);
		Assert.assertSame(ElasticTweenEquation.OUT, TweenEquations.ELASTIC_OUT);
		Assert.assertSame(ElasticTweenEquation.IN_OUT, TweenEquations.ELASTIC_IN_OUT);
	}

	@Test
	public void findsEveryEquationByName() {
		for (final TweenEquation equation : new TweenEquation[] {TweenEquations.LINEAR, TweenEquations.QUAD_IN, TweenEquations.QUAD_OUT, TweenEquations.QUAD_IN_OUT, TweenEquations.CUBIC_IN, TweenEquations.CUBIC_OUT, TweenEquations.CUBIC_IN_OUT, TweenEquations.QUART_IN, TweenEquations.QUART_OUT, TweenEquations.QUART_IN_OUT, TweenEquations.QUINT_IN, TweenEquations.QUINT_OUT, TweenEquations.QUINT_IN_OUT, TweenEquations.CIRC_IN, TweenEquations.CIRC_OUT, TweenEquations.CIRC_IN_OUT, TweenEquations.SINE_IN, TweenEquations.SINE_OUT, TweenEquations.SINE_IN_OUT, TweenEquations.EXPO_IN, TweenEquations.EXPO_OUT, TweenEquations.EXPO_IN_OUT, TweenEquations.BACK_IN, TweenEquations.BACK_OUT, TweenEquations.BACK_IN_OUT, TweenEquations.BOUNCE_IN, TweenEquations.BOUNCE_OUT, TweenEquations.BOUNCE_IN_OUT, TweenEquations.ELASTIC_IN, TweenEquations.ELASTIC_OUT, TweenEquations.ELASTIC_IN_OUT}) {
			Assert.assertSame(equation, TweenEquations.of(equation.toString()));
		}
	}

	@Test
	public void findsATunedEquationByItsName() {
		final BackTweenEquation back = (BackTweenEquation) TweenEquations.of(BackTweenEquation.OUT.overshoot(2.5F).toString());
		final ElasticTweenEquation elastic = (ElasticTweenEquation) TweenEquations.of(ElasticTweenEquation.IN_OUT.amplitude(2F).period(0.5F).toString());
		Assert.assertEquals(BackTweenEquation.OUT.overshoot(2.5F).compute(0.5F), back.compute(0.5F), 0F);
		Assert.assertEquals("Back.OUT(2.5)", back.toString());
		Assert.assertEquals(ElasticTweenEquation.IN_OUT.amplitude(2F).period(0.5F).compute(0.3F), elastic.compute(0.3F), 0F);
		Assert.assertEquals("Elastic.IN_OUT(0.5, 2.0)", elastic.toString());
	}

	@Test
	public void findsNothingFromAnUnknownName() {
		Assert.assertNull(TweenEquations.of("Quad.SIDEWAYS"));
		Assert.assertNull(TweenEquations.of("quad.in"));
		Assert.assertNull(TweenEquations.of("Quad.IN(2.0)"));
		Assert.assertNull(TweenEquations.of("Back.IN(2.0, 1.0)"));
	}

}