package dev.joid.lib.animation.tween;

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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TweenEquations {

	public static final LinearTweenEquation LINEAR = LinearTweenEquation.INOUT;

	public static final QuadTweenEquation QUAD_IN    = QuadTweenEquation.IN;
	public static final QuadTweenEquation QUAD_OUT   = QuadTweenEquation.OUT;
	public static final QuadTweenEquation QUAD_INOUT = QuadTweenEquation.INOUT;

	public static final CubicTweenEquation CUBIC_IN    = CubicTweenEquation.IN;
	public static final CubicTweenEquation CUBIC_OUT   = CubicTweenEquation.OUT;
	public static final CubicTweenEquation CUBIC_INOUT = CubicTweenEquation.INOUT;

	public static final QuartTweenEquation QUART_IN    = QuartTweenEquation.IN;
	public static final QuartTweenEquation QUART_OUT   = QuartTweenEquation.OUT;
	public static final QuartTweenEquation QUART_INOUT = QuartTweenEquation.INOUT;

	public static final QuintTweenEquation QUINT_IN    = QuintTweenEquation.IN;
	public static final QuintTweenEquation QUINT_OUT   = QuintTweenEquation.OUT;
	public static final QuintTweenEquation QUINT_INOUT = QuintTweenEquation.INOUT;

	public static final CircTweenEquation CIRC_IN    = CircTweenEquation.IN;
	public static final CircTweenEquation CIRC_OUT   = CircTweenEquation.OUT;
	public static final CircTweenEquation CIRC_INOUT = CircTweenEquation.INOUT;

	public static final SineTweenEquation SINE_IN    = SineTweenEquation.IN;
	public static final SineTweenEquation SINE_OUT   = SineTweenEquation.OUT;
	public static final SineTweenEquation SINE_INOUT = SineTweenEquation.INOUT;

	public static final ExpoTweenEquation EXPO_IN    = ExpoTweenEquation.IN;
	public static final ExpoTweenEquation EXPO_OUT   = ExpoTweenEquation.OUT;
	public static final ExpoTweenEquation EXPO_INOUT = ExpoTweenEquation.INOUT;

	public static final BackTweenEquation BACK_IN    = BackTweenEquation.IN;
	public static final BackTweenEquation BACK_OUT   = BackTweenEquation.OUT;
	public static final BackTweenEquation BACK_INOUT = BackTweenEquation.INOUT;

	public static final BounceTweenEquation BOUNCE_IN    = BounceTweenEquation.IN;
	public static final BounceTweenEquation BOUNCE_OUT   = BounceTweenEquation.OUT;
	public static final BounceTweenEquation BOUNCE_INOUT = BounceTweenEquation.INOUT;

	public static final ElasticTweenEquation ELASTIC_IN    = ElasticTweenEquation.IN;
	public static final ElasticTweenEquation ELASTIC_OUT   = ElasticTweenEquation.OUT;
	public static final ElasticTweenEquation ELASTIC_INOUT = ElasticTweenEquation.INOUT;

	private static final TweenEquation[] EASINGS = {
		TweenEquations.LINEAR, TweenEquations.QUAD_IN, TweenEquations.QUAD_OUT, TweenEquations.QUAD_INOUT, TweenEquations.CUBIC_IN,
		TweenEquations.CUBIC_OUT, TweenEquations.CUBIC_INOUT, TweenEquations.QUART_IN, TweenEquations.QUART_OUT, TweenEquations.QUART_INOUT,
		TweenEquations.QUINT_IN, TweenEquations.QUINT_OUT, TweenEquations.QUINT_INOUT, TweenEquations.CIRC_IN, TweenEquations.CIRC_OUT,
		TweenEquations.CIRC_INOUT, TweenEquations.SINE_IN, TweenEquations.SINE_OUT, TweenEquations.SINE_INOUT, TweenEquations.EXPO_IN,
		TweenEquations.EXPO_OUT, TweenEquations.EXPO_INOUT, TweenEquations.BACK_IN, TweenEquations.BACK_OUT, TweenEquations.BACK_INOUT,
		TweenEquations.BOUNCE_IN, TweenEquations.BOUNCE_OUT, TweenEquations.BOUNCE_INOUT, TweenEquations.ELASTIC_IN, TweenEquations.ELASTIC_OUT,
		TweenEquations.ELASTIC_INOUT
	};

	public static TweenEquation of(final @NonNull String name) {
		for (final TweenEquation easing : TweenEquations.EASINGS) {
			if (easing.isValueOf(name)) {
				return easing;
			}
		}
		return null;
	}

}