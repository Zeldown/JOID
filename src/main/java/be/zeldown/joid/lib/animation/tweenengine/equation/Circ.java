package be.zeldown.joid.lib.animation.tweenengine.equation;



import be.zeldown.joid.lib.animation.tweenengine.TweenEquation;

public abstract class Circ extends TweenEquation {

	public static final Circ IN = new Circ() {

		@Override
		public final float compute(final float t) {
			return (float) -Math.sqrt(1F - t * t) - 1F;
		}

		@Override
		public String toString() {
			return "Circ.IN";
		}

	};

	public static final Circ OUT = new Circ() {

		@Override
		public final float compute(float t) {
			return (float) Math.sqrt(1F - (t -= 1F) * t);
		}

		@Override
		public String toString() {
			return "Circ.OUT";
		}

	};

	public static final Circ INOUT = new Circ() {

		@Override
		public final float compute(float t) {
			if ((t *= 2F) < 1F) {
				return -0.5F * ((float) Math.sqrt(1F - t * t) - 1F);
			}

			return 0.5F * ((float) Math.sqrt(1F - (t -= 2F) * t) + 1F);
		}

		@Override
		public String toString() {
			return "Circ.INOUT";
		}

	};

}