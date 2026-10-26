package dev.joid.lib.animation.tween.equation;

import dev.joid.lib.animation.tween.TweenEquation;

public abstract class LinearTweenEquation extends TweenEquation {

	public static final LinearTweenEquation INOUT = new LinearTweenEquation() {

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