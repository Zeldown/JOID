package dev.joid.lib.animation.tween.equation;

import dev.joid.lib.animation.tween.TweenEquation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class CubicTweenEquation extends TweenEquation {

	public static final CubicTweenEquation IN = new CubicTweenEquation() {

		@Override
		public float compute(final float t) {
			return t * t * t;
		}

		@Override
		public String toString() {
			return "Cubic.IN";
		}

	};

	public static final CubicTweenEquation OUT = new CubicTweenEquation() {

		@Override
		public float compute(final float time) {
			float t = time;
			return (t -= 1F) * t * t + 1F;
		}

		@Override
		public String toString() {
			return "Cubic.OUT";
		}

	};

	public static final CubicTweenEquation IN_OUT = new CubicTweenEquation() {

		@Override
		public float compute(final float time) {
			float t = time;
			if ((t *= 2F) < 1F) {
				return 0.5F * t * t * t;
			}

			return 0.5F * ((t -= 2F) * t * t + 2F);
		}

		@Override
		public String toString() {
			return "Cubic.IN_OUT";
		}

	};

}