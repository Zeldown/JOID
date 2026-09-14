package be.zeldown.joid.lib.animation.tweenengine.equation;



import be.zeldown.joid.lib.animation.tweenengine.TweenEquation;

public abstract class Quart extends TweenEquation {

	public static final Quart IN = new Quart() {

		@Override
		public final float compute(final float t) {
			return t * t * t * t;
		}

		@Override
		public String toString() {
			return "Quart.IN";
		}

	};

	public static final Quart OUT = new Quart() {

		@Override
		public final float compute(float t) {
			return -((t -= 1F) * t * t * t - 1F);
		}

		@Override
		public String toString() {
			return "Quart.OUT";
		}

	};

	public static final Quart INOUT = new Quart() {

		@Override
		public final float compute(float t) {
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