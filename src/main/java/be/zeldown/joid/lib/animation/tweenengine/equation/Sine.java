package be.zeldown.joid.lib.animation.tweenengine.equation;



import be.zeldown.joid.lib.animation.tweenengine.TweenEquation;

public abstract class Sine extends TweenEquation {

	private static final float PI = 3.14159265F;

	public static final Sine IN = new Sine() {

		@Override
		public final float compute(final float t) {
			return (float) -Math.cos(t * (Sine.PI / 2D)) + 1F;
		}

		@Override
		public String toString() {
			return "Sine.IN";
		}

	};

	public static final Sine OUT = new Sine() {

		@Override
		public final float compute(final float t) {
			return (float) Math.sin(t * (Sine.PI / 2D));
		}

		@Override
		public String toString() {
			return "Sine.OUT";
		}

	};

	public static final Sine INOUT = new Sine() {

		@Override
		public final float compute(final float t) {
			return -0.5F * ((float) Math.cos(Sine.PI * t) - 1F);
		}

		@Override
		public String toString() {
			return "Sine.INOUT";
		}

	};

}