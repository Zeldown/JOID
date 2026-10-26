package dev.joid.lib.animation.tween.equation;

import dev.joid.lib.animation.tween.TweenEquation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class QuartTweenEquation extends TweenEquation {

	public static final QuartTweenEquation IN = new QuartTweenEquation() {

		@Override
		public final float compute(final float t) {
			return t * t * t * t;
		}

		@Override
		public String toString() {
			return "Quart.IN";
		}

	};

	public static final QuartTweenEquation OUT = new QuartTweenEquation() {

		@Override
		public final float compute(final float time) {
			float t = time;
			return -((t -= 1F) * t * t * t - 1F);
		}

		@Override
		public String toString() {
			return "Quart.OUT";
		}

	};

	public static final QuartTweenEquation INOUT = new QuartTweenEquation() {

		@Override
		public final float compute(final float time) {
			float t = time;
			if ((t *= 2F) < 1F) {
				return 0.5F * t * t * t * t;
			}

			return -0.5F * ((t -= 2F) * t * t * t - 2F);
		}

		@Override
		public String toString() {
			return "Quart.INOUT";
		}

	};

}