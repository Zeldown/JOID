package dev.joid.lib.animation.tween.equation;

import dev.joid.lib.animation.tween.TweenEquation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ElasticTweenEquation extends TweenEquation {

	public static final ElasticTweenEquation IN    = ElasticTweenEquation.in(0F, 0.3F);
	public static final ElasticTweenEquation OUT   = ElasticTweenEquation.out(0F, 0.3F);
	public static final ElasticTweenEquation INOUT = ElasticTweenEquation.inOut(0F, 0.3F * 1.5F);

	protected final float period;
	protected final float amplitude;

	public abstract @NonNull ElasticTweenEquation period(final float period);
	public abstract @NonNull ElasticTweenEquation amplitude(final float amplitude);

	private static ElasticTweenEquation in(final float amplitude, final float period) {
		return new ElasticTweenEquation(period, amplitude) {

			@Override
			public final float compute(final float time) {
				float t = time;
				float a = super.amplitude;
				final float p = super.period;
				if (t == 0F) {
					return 0F;
				}

				if (t == 1F) {
					return 1F;
				}

				final float s;
				if (a < 1F) {
					a = 1F;
					s = p / 4F;
				} else {
					s = (float) (p / (2D * Math.PI) * Math.asin(1F / a));
				}

				return -(a * (float) Math.pow(2D, 10D * (t -= 1F)) * (float) Math.sin((t - s) * (2D * Math.PI) / p));
			}

			@Override
			public @NonNull ElasticTweenEquation period(final float period) {
				return ElasticTweenEquation.in(super.amplitude, period);
			}

			@Override
			public @NonNull ElasticTweenEquation amplitude(final float amplitude) {
				return ElasticTweenEquation.in(amplitude, super.period);
			}

			@Override
			public String toString() {
				return "Elastic.IN";
			}

		};
	}

	private static ElasticTweenEquation out(final float amplitude, final float period) {
		return new ElasticTweenEquation(period, amplitude) {

			@Override
			public final float compute(final float t) {
				float a = super.amplitude;
				final float p = super.period;
				if (t == 0F) {
					return 0F;
				}

				if (t == 1F) {
					return 1F;
				}

				final float s;
				if (a < 1F) {
					a = 1F;
					s = p / 4F;
				} else {
					s = (float) (p / (2D * Math.PI) * Math.asin(1F / a));
				}

				return a * (float) Math.pow(2D, -10D * t) * (float) Math.sin((t - s) * (2D * Math.PI) / p) + 1F;
			}

			@Override
			public @NonNull ElasticTweenEquation period(final float period) {
				return ElasticTweenEquation.out(super.amplitude, period);
			}

			@Override
			public @NonNull ElasticTweenEquation amplitude(final float amplitude) {
				return ElasticTweenEquation.out(amplitude, super.period);
			}

			@Override
			public String toString() {
				return "Elastic.OUT";
			}

		};
	}

	private static ElasticTweenEquation inOut(final float amplitude, final float period) {
		return new ElasticTweenEquation(period, amplitude) {

			@Override
			public final float compute(final float time) {
				float t = time;
				float a = super.amplitude;
				final float p = super.period;
				if (t == 0F) {
					return 0F;
				}

				if ((t *= 2F) == 2F) {
					return 1F;
				}

				final float s;
				if (a < 1F) {
					a = 1F;
					s = p / 4F;
				} else {
					s = (float) (p / (2D * Math.PI) * Math.asin(1F / a));
				}

				if (t < 1F) {
					return -0.5F * (a * (float) Math.pow(2D, 10D * (t -= 1F)) * (float) Math.sin((t - s) * (2D * Math.PI) / p));
				}

				return a * (float) Math.pow(2D, -10D * (t -= 1F)) * (float) Math.sin((t - s) * (2D * Math.PI) / p) * 0.5F + 1F;
			}

			@Override
			public @NonNull ElasticTweenEquation period(final float period) {
				return ElasticTweenEquation.inOut(super.amplitude, period);
			}

			@Override
			public @NonNull ElasticTweenEquation amplitude(final float amplitude) {
				return ElasticTweenEquation.inOut(amplitude, super.period);
			}

			@Override
			public String toString() {
				return "Elastic.INOUT";
			}

		};
	}

}