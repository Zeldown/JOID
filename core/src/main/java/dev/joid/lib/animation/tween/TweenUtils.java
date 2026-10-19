package dev.joid.lib.animation.tween;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TweenUtils {

	private static final TweenEquation[] EASINGS = {
		TweenEquations.LINEAR, TweenEquations.QUAD_IN, TweenEquations.QUAD_OUT, TweenEquations.QUAD_INOUT, TweenEquations.CUBIC_IN,
		TweenEquations.CUBIC_OUT, TweenEquations.CUBIC_INOUT, TweenEquations.QUART_IN, TweenEquations.QUART_OUT, TweenEquations.QUART_INOUT,
		TweenEquations.QUINT_IN, TweenEquations.QUINT_OUT, TweenEquations.QUINT_INOUT, TweenEquations.CIRC_IN, TweenEquations.CIRC_OUT,
		TweenEquations.CIRC_INOUT, TweenEquations.SINE_IN, TweenEquations.SINE_OUT, TweenEquations.SINE_INOUT, TweenEquations.EXPO_IN,
		TweenEquations.EXPO_OUT, TweenEquations.EXPO_INOUT, TweenEquations.BACK_IN, TweenEquations.BACK_OUT, TweenEquations.BACK_INOUT,
		TweenEquations.BOUNCE_IN, TweenEquations.BOUNCE_OUT, TweenEquations.BOUNCE_INOUT, TweenEquations.ELASTIC_IN, TweenEquations.ELASTIC_OUT,
		TweenEquations.ELASTIC_INOUT
	};

	public static TweenEquation parseEasing(final @NonNull String easingName) {
		for (final TweenEquation easing : TweenUtils.EASINGS) {
			if (easing.isValueOf(easingName)) {
				return easing;
			}
		}
		return null;
	}

}