package be.zeldown.joid.lib.animation.tweenengine.equation;



import be.zeldown.joid.lib.animation.tweenengine.TweenEquation;

public abstract class Expo extends TweenEquation {

	public static final Expo IN = new Expo() {

		@Override
		public final float compute(final float t) {
			return (t == 0F) ? 0F : (float) Math.pow(2D, 10D * (t - 1D));
		}

		@Override
		public String toString() {
			return "Expo.IN";
		}

	};

	public static final Expo OUT = new Expo() {

		@Override
		public final float compute(final float t) {
			return (t == 1F) ? 1F : -(float) Math.pow(2D, -10D * t) + 1F;
		}

		@Override
		public String toString() {
			return "Expo.OUT";
		}

	};

	public static final Expo INOUT = new Expo() {

		@Override
		public final float compute(float t) {
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