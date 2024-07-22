package be.zeldown.joid.lib.animation.tweenengine.equation;



import be.zeldown.joid.lib.animation.tweenengine.TweenEquation;

public abstract class Quint extends TweenEquation {

	public static final Quint IN = new Quint() {

		@Override
		public final float compute(final float t) {
			return t * t * t * t * t;
		}

		@Override
		public String toString() {
			return "Quint.IN";
		}

	};

	public static final Quint OUT = new Quint() {

		@Override
		public final float compute(float t) {
			return (t -= 1F) * t * t * t * t + 1F;
		}

		@Override
		public String toString() {
			return "Quint.OUT";
		}

	};

	public static final Quint INOUT = new Quint() {

		@Override
		public final float compute(float t) {
			if ((t *= 2F) < 1F) {
				return 0.5F * t * t * t * t * t;
			}

			return 0.5F * ((t -= 2F) * t * t * t * t + 2F);
		}

		@Override
		public String toString() {
			return "Quint.INOUT";
		}

	};

}