package dev.joid.lib.animation.tween.equation;

import dev.joid.lib.animation.tween.TweenEquation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class LinearTweenEquation extends TweenEquation {

	public static final LinearTweenEquation IN_OUT = new LinearTweenEquation() {

		@Override
		public float compute(final float t) {
			return t;
		}

		@Override
		public String toString() {
			return "Linear.IN_OUT";
		}

	};

}