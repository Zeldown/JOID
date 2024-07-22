package be.zeldown.joid.lib.animation.tweenengine.equation;



import be.zeldown.joid.lib.animation.tweenengine.TweenEquation;

public abstract class Linear extends TweenEquation {

	public static final Linear INOUT = new Linear() {

		@Override
		public float compute(final float t) {
			return t;
		}

		@Override
		public String toString() {
			return "Linear.INOUT";
		}

	};

}