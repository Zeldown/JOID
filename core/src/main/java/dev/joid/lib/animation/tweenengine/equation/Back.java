package dev.joid.lib.animation.tweenengine.equation;



import dev.joid.lib.animation.tweenengine.TweenEquation;
import lombok.NonNull;

public abstract class Back extends TweenEquation {

	public static final Back IN    = Back.in(1.70158F);
	public static final Back OUT   = Back.out(1.70158F);
	public static final Back INOUT = Back.inOut(1.70158F);

	protected final float bounce;

	protected Back(final float bounce) {
		this.bounce = bounce;
	}

	public abstract @NonNull Back s(final float s);

	private static Back in(final float bounce) {
		return new Back(bounce) {

			@Override
			public final float compute(final float t) {
				final float s = this.bounce;
				return t * t * ((s + 1) * t - s);
			}

			@Override
			public @NonNull Back s(final float s) {
				return Back.in(s);
			}

			@Override
			public String toString() {
				return "Back.IN";
			}

		};
	}

	private static Back out(final float bounce) {
		return new Back(bounce) {

			@Override
			public final float compute(final float time) {
				float t = time;
				final float s = this.bounce;
				return (t -= 1) * t * ((s + 1) * t + s) + 1;
			}

			@Override
			public @NonNull Back s(final float s) {
				return Back.out(s);
			}

			@Override
			public String toString() {
				return "Back.OUT";
			}

		};
	}

	private static Back inOut(final float bounce) {
		return new Back(bounce) {

			@Override
			public final float compute(final float time) {
				float t = time;
				float s = this.bounce;
				if ((t *= 2) < 1) {
					return 0.5F * (t * t * (((s *= (1.525F)) + 1) * t - s));
				}

				return 0.5F * ((t -= 2) * t * (((s *= (1.525F)) + 1) * t + s) + 2);
			}

			@Override
			public @NonNull Back s(final float s) {
				return Back.inOut(s);
			}

			@Override
			public String toString() {
				return "Back.INOUT";
			}

		};
	}

}