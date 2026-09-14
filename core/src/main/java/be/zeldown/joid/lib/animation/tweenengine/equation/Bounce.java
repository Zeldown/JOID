package be.zeldown.joid.lib.animation.tweenengine.equation;



import be.zeldown.joid.lib.animation.tweenengine.TweenEquation;

public abstract class Bounce extends TweenEquation {

	public static final Bounce IN = new Bounce() {

		@Override
		public final float compute(final float t) {
			return 1 - Bounce.OUT.compute(1 - t);
		}

		@Override
		public String toString() {
			return "Bounce.IN";
		}

	};

	public static final Bounce OUT = new Bounce() {

		@Override
		public final float compute(float t) {
			if (t < (1F / 2.75F)) {
				return 7.5625F * t * t;
			} else if (t < (2F / 2.75F)) {
				return 7.5625F * (t -= (1.5F / 2.75F)) * t + 0.75F;
			} else if (t < (2.5 / 2.75)) {
				return 7.5625F * (t -= (2.25F / 2.75f)) * t + 0.9375F;
			} else {
				return 7.5625F * (t -= (2.625F / 2.75f)) * t + 0.984375F;
			}
		}

		@Override
		public String toString() {
			return "Bounce.OUT";
		}

	};

	public static final Bounce INOUT = new Bounce() {

		@Override
		public final float compute(final float t) {
			if (t < 0.5F) {
				return Bounce.IN.compute(t * 2F) * 0.5F;
			} else {
				return Bounce.OUT.compute(t * 2F - 1F) * 0.5F + 0.5F;
			}
		}

		@Override
		public String toString() {
			return "Bounce.INOUT";
		}

	};

}