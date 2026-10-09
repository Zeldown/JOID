package dev.joid.lib.animation.tween.equation;



import dev.joid.lib.animation.tween.TweenEquation;

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