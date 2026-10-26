package dev.joid.lib.animation.tween.equation;

import dev.joid.lib.animation.tween.TweenEquation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BounceTweenEquation extends TweenEquation {

	public static final BounceTweenEquation IN = new BounceTweenEquation() {

		@Override
		public final float compute(final float t) {
			return 1F - BounceTweenEquation.OUT.compute(1F - t);
		}

		@Override
		public String toString() {
			return "Bounce.IN";
		}

	};

	public static final BounceTweenEquation OUT = new BounceTweenEquation() {

		@Override
		public final float compute(final float time) {
			float t = time;
			if (t < (1F / 2.75F)) {
				return 7.5625F * t * t;
			} else if (t < (2F / 2.75F)) {
				return 7.5625F * (t -= (1.5F / 2.75F)) * t + 0.75F;
			} else if (t < (2.5F / 2.75F)) {
				return 7.5625F * (t -= (2.25F / 2.75F)) * t + 0.9375F;
			} else {
				return 7.5625F * (t -= (2.625F / 2.75F)) * t + 0.984375F;
			}
		}

		@Override
		public String toString() {
			return "Bounce.OUT";
		}

	};

	public static final BounceTweenEquation INOUT = new BounceTweenEquation() {

		@Override
		public final float compute(final float t) {
			if (t < 0.5F) {
				return BounceTweenEquation.IN.compute(t * 2F) * 0.5F;
			} else {
				return BounceTweenEquation.OUT.compute(t * 2F - 1F) * 0.5F + 0.5F;
			}
		}

		@Override
		public String toString() {
			return "Bounce.INOUT";
		}

	};

}