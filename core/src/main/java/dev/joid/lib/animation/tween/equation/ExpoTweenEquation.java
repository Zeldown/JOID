package dev.joid.lib.animation.tween.equation;

import dev.joid.lib.animation.tween.TweenEquation;

public abstract class ExpoTweenEquation extends TweenEquation {

	public static final ExpoTweenEquation IN = new ExpoTweenEquation() {

		@Override
		public final float compute(final float t) {
			return (t == 0F) ? 0F : (float) Math.pow(2D, 10D * (t - 1D));
		}

		@Override
		public String toString() {
			return "Expo.IN";
		}

	};

	public static final ExpoTweenEquation OUT = new ExpoTweenEquation() {

		@Override
		public final float compute(final float t) {
			return (t == 1F) ? 1F : -(float) Math.pow(2D, -10D * t) + 1F;
		}

		@Override
		public String toString() {
			return "Expo.OUT";
		}

	};

	public static final ExpoTweenEquation INOUT = new ExpoTweenEquation() {

		@Override
		public final float compute(final float time) {
			float t = time;
			if (t == 0F) {
				return 0F;
			}

			if (t == 1F) {
				return 1F;
			}

			if ((t *= 2F) < 1F) {
				return 0.5F * (float) Math.pow(2D, 10D * (t - 1D));
			}

			return 0.5F * (-(float) Math.pow(2D, -10D * --t) + 2F);
		}

		@Override
		public String toString() {
			return "Expo.INOUT";
		}

	};

}