package be.zeldown.joid.lib.animation.tweenengine.equation;



import be.zeldown.joid.lib.animation.tweenengine.TweenEquation;

public abstract class Back extends TweenEquation {

	public static final Back IN = new Back() {

		@Override
		public final float compute(final float t) {
			final float s = this.bounce;
			return t * t * ((s + 1) * t - s);
		}

		@Override
		public String toString() {
			return "Back.IN";
		}

	};

	public static final Back OUT = new Back() {

		@Override
		public final float compute(float t) {
			final float s = this.bounce;
			return (t -= 1) * t * ((s + 1) * t + s) + 1;
		}

		@Override
		public String toString() {
			return "Back.OUT";
		}

	};

	public static final Back INOUT = new Back() {

		@Override
		public final float compute(float t) {
			float s = this.bounce;
			if ((t *= 2) < 1) {
				return 0.5f * (t * t * (((s *= (1.525f)) + 1) * t - s));
			}

			return 0.5f * ((t -= 2) * t * (((s *= (1.525f)) + 1) * t + s) + 2);
		}

		@Override
		public String toString() {
			return "Back.INOUT";
		}

	};

	// -------------------------------------------------------------------------

	protected float bounce = 1.70158f;

	public Back s(final float s) {
		this.bounce = s;
		return this;
	}

}