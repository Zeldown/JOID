package dev.joid.lib.animation.tween.equation;

import dev.joid.lib.animation.tween.TweenEquation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class QuadTweenEquation extends TweenEquation {

	public static final QuadTweenEquation IN = new QuadTweenEquation() {

		@Override
		public float compute(final float t) {
			return t * t;
		}

		@Override
		public String toString() {
			return "Quad.IN";
		}

	};

	public static final QuadTweenEquation OUT = new QuadTweenEquation() {

		@Override
		public float compute(final float t) {
			return -t * (t - 2F);
		}

		@Override
		public String toString() {
			return "Quad.OUT";
		}

	};

	public static final QuadTweenEquation IN_OUT = new QuadTweenEquation() {

		@Override
		public float compute(final float time) {
			float t = time;
			if ((t *= 2F) < 1F) {
				return 0.5F * t * t;
			}

			return -0.5F * ((--t) * (t - 2F) - 1F);
		}

		@Override
		public String toString() {
			return "Quad.IN_OUT";
		}

	};

}