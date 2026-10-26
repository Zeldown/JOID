package dev.joid.lib.animation.tween.equation;

import dev.joid.lib.animation.tween.TweenEquation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class CircTweenEquation extends TweenEquation {

	public static final CircTweenEquation IN = new CircTweenEquation() {

		@Override
		public float compute(final float t) {
			return (float) -(Math.sqrt(1F - t * t) - 1F);
		}

		@Override
		public String toString() {
			return "Circ.IN";
		}

	};

	public static final CircTweenEquation OUT = new CircTweenEquation() {

		@Override
		public float compute(final float time) {
			float t = time;
			return (float) Math.sqrt(1F - (t -= 1F) * t);
		}

		@Override
		public String toString() {
			return "Circ.OUT";
		}

	};

	public static final CircTweenEquation INOUT = new CircTweenEquation() {

		@Override
		public float compute(final float time) {
			float t = time;
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