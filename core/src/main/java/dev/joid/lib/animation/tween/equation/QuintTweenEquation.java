package dev.joid.lib.animation.tween.equation;

import dev.joid.lib.animation.tween.TweenEquation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class QuintTweenEquation extends TweenEquation {

	public static final QuintTweenEquation IN = new QuintTweenEquation() {

		@Override
		public float compute(final float t) {
			return t * t * t * t * t;
		}

		@Override
		public String toString() {
			return "Quint.IN";
		}

	};

	public static final QuintTweenEquation OUT = new QuintTweenEquation() {

		@Override
		public float compute(final float time) {
			float t = time;
			return (t -= 1F) * t * t * t * t + 1F;
		}

		@Override
		public String toString() {
			return "Quint.OUT";
		}

	};

	public static final QuintTweenEquation IN_OUT = new QuintTweenEquation() {

		@Override
		public float compute(final float time) {
			float t = time;
			if ((t *= 2F) < 1F) {
				return 0.5F * t * t * t * t * t;
			}

			return 0.5F * ((t -= 2F) * t * t * t * t + 2F);
		}

		@Override
		public String toString() {
			return "Quint.IN_OUT";
		}

	};

}