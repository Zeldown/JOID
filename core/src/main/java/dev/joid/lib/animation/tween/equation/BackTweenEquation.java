package dev.joid.lib.animation.tween.equation;

import dev.joid.lib.animation.tween.TweenEquation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BackTweenEquation extends TweenEquation {

	public static final BackTweenEquation IN     = BackTweenEquation.in(1.70158F);
	public static final BackTweenEquation OUT    = BackTweenEquation.out(1.70158F);
	public static final BackTweenEquation IN_OUT = BackTweenEquation.inOut(1.70158F);

	protected final float overshoot;

	public abstract @NonNull BackTweenEquation overshoot(final float overshoot);

	private static String name(final String name, final BackTweenEquation easing, final BackTweenEquation standard) {
		return easing.overshoot == standard.overshoot ? name : name + "(" + easing.overshoot + ")";
	}

	private static BackTweenEquation in(final float overshoot) {
		return new BackTweenEquation(overshoot) {

			@Override
			public float compute(final float t) {
				final float s = super.overshoot;
				return t * t * ((s + 1F) * t - s);
			}

			@Override
			public @NonNull BackTweenEquation overshoot(final float overshoot) {
				return BackTweenEquation.in(overshoot);
			}

			@Override
			public String toString() {
				return BackTweenEquation.name("Back.IN", this, BackTweenEquation.IN);
			}

		};
	}

	private static BackTweenEquation out(final float overshoot) {
		return new BackTweenEquation(overshoot) {

			@Override
			public float compute(final float time) {
				float t = time;
				final float s = super.overshoot;
				return (t -= 1F) * t * ((s + 1F) * t + s) + 1F;
			}

			@Override
			public @NonNull BackTweenEquation overshoot(final float overshoot) {
				return BackTweenEquation.out(overshoot);
			}

			@Override
			public String toString() {
				return BackTweenEquation.name("Back.OUT", this, BackTweenEquation.OUT);
			}

		};
	}

	private static BackTweenEquation inOut(final float overshoot) {
		return new BackTweenEquation(overshoot) {

			@Override
			public float compute(final float time) {
				float t = time;
				float s = super.overshoot;
				if ((t *= 2F) < 1F) {
					return 0.5F * (t * t * (((s *= 1.525F) + 1F) * t - s));
				}

				return 0.5F * ((t -= 2F) * t * (((s *= 1.525F) + 1F) * t + s) + 2F);
			}

			@Override
			public @NonNull BackTweenEquation overshoot(final float overshoot) {
				return BackTweenEquation.inOut(overshoot);
			}

			@Override
			public String toString() {
				return BackTweenEquation.name("Back.IN_OUT", this, BackTweenEquation.IN_OUT);
			}

		};
	}

}