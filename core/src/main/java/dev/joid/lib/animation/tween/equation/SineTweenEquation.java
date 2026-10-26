package dev.joid.lib.animation.tween.equation;

import dev.joid.lib.animation.tween.TweenEquation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SineTweenEquation extends TweenEquation {

	public static final SineTweenEquation IN = new SineTweenEquation() {

		@Override
		public float compute(final float t) {
			return (float) -Math.cos(t * (Math.PI / 2D)) + 1F;
		}

		@Override
		public String toString() {
			return "Sine.IN";
		}

	};

	public static final SineTweenEquation OUT = new SineTweenEquation() {

		@Override
		public float compute(final float t) {
			return (float) Math.sin(t * (Math.PI / 2D));
		}

		@Override
		public String toString() {
			return "Sine.OUT";
		}

	};

	public static final SineTweenEquation IN_OUT = new SineTweenEquation() {

		@Override
		public float compute(final float t) {
			return -0.5F * ((float) Math.cos(Math.PI * t) - 1F);
		}

		@Override
		public String toString() {
			return "Sine.IN_OUT";
		}

	};

}