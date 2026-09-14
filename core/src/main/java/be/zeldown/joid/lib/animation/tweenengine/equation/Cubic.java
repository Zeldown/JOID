package be.zeldown.joid.lib.animation.tweenengine.equation;



import be.zeldown.joid.lib.animation.tweenengine.TweenEquation;

public abstract class Cubic extends TweenEquation {

	public static final Cubic IN = new Cubic() {

		@Override
		public final float compute(final float t) {
			return t * t * t;
		}

		@Override
		public String toString() {
			return "Cubic.IN";
		}

	};

	public static final Cubic OUT = new Cubic() {

		@Override
		public final float compute(float t) {
			return (t -= 1F) * t * t + 1F;
		}

		@Override
		public String toString() {
			return "Cubic.OUT";
		}

	};

	public static final Cubic INOUT = new Cubic() {

		@Override
		public final float compute(float t) {
			if ((t *= 2F) < 1F) {
				return 0.5F * t * t * t;
			}

			return 0.5F * ((t -= 2F) * t * t + 2F);
		}

		@Override
		public String toString() {
			return "Cubic.INOUT";
		}

	};

}