package dev.joid.lib.animation.tweenengine;



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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TweenUtils {

	private static TweenEquation[] easings;

	public static TweenEquation parseEasing(final String easingName) {
		if (TweenUtils.easings == null) {
			TweenUtils.easings = new TweenEquation[] {
				Linear.INOUT, Quad.IN, Quad.OUT, Quad.INOUT, Cubic.IN,
				Cubic.OUT, Cubic.INOUT, Quart.IN, Quart.OUT, Quart.INOUT, Quint.IN, Quint.OUT,
				Quint.INOUT, Circ.IN, Circ.OUT, Circ.INOUT, Sine.IN, Sine.OUT, Sine.INOUT, Expo.IN,
				Expo.OUT, Expo.INOUT, Back.IN, Back.OUT, Back.INOUT, Bounce.IN, Bounce.OUT, Bounce.INOUT,
				Elastic.IN, Elastic.OUT, Elastic.INOUT
			};
		}

		for (final TweenEquation easing : TweenUtils.easings) {
			if (easingName.equals(easing.toString())) {
				return easing;
			}
		}

		return null;
	}

}