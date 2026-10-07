package dev.joid.lib.animation.tweenengine.equation;



import dev.joid.lib.animation.tweenengine.TweenEquation;
import lombok.NonNull;

public abstract class Elastic extends TweenEquation {

	private static final float PI = 3.14159265F;

	public static final Elastic IN    = Elastic.in(0F, 0.3F);
	public static final Elastic OUT   = Elastic.out(0F, 0.3F);
	public static final Elastic INOUT = Elastic.inOut(0F, 0.3F * 1.5F);

	protected final float a;
	protected final float p;

	protected Elastic(final float a, final float p) {
		this.a = a;
		this.p = p;
	}

	public abstract @NonNull Elastic a(final float a);
	public abstract @NonNull Elastic p(final float p);

	private static Elastic in(final float amplitude, final float period) {
		return new Elastic(amplitude, period) {

			@Override
			public final float compute(final float time) {
				float t = time;
				float a = this.a;
				final float p = this.p;
				if (t == 0F) {
					return 0F;
				}

				if (t == 1F) {
					return 1F;
				}

				float s;
				if (a < 1F) {
					a = 1F;
					s = p / 4F;
				} else {
					s = p / (2F * Elastic.PI) * (float) Math.asin(1F / a);
				}

				return -(a * (float) Math.pow(2D, 10D * (t -= 1D)) * (float) Math.sin((t - s) * (2D * Elastic.PI) / p));
			}

			@Override
			public @NonNull Elastic a(final float a) {
				return Elastic.in(a, this.p);
			}

			@Override
			public @NonNull Elastic p(final float p) {
				return Elastic.in(this.a, p);
			}

			@Override
			public String toString() {
				return "Elastic.IN";
			}

		};
	}

	private static Elastic out(final float amplitude, final float period) {
		return new Elastic(amplitude, period) {

			@Override
			public final float compute(final float t) {
				float a = this.a;
				final float p = this.p;
				if (t == 0F) {
					return 0F;
				}

				if (t == 1F) {
					return 1F;
				}

				float s;
				if (a < 1F) {
					a = 1F;
					s = p / 4F;
				} else {
					s = p / (2F * Elastic.PI) * (float) Math.asin(1F / a);
				}

				return a * (float) Math.pow(2D, -10D * t) * (float) Math.sin((t - s) * (2D * Elastic.PI) / p) + 1F;
			}

			@Override
			public @NonNull Elastic a(final float a) {
				return Elastic.out(a, this.p);
			}

			@Override
			public @NonNull Elastic p(final float p) {
				return Elastic.out(this.a, p);
			}

			@Override
			public String toString() {
				return "Elastic.OUT";
			}

		};
	}

	private static Elastic inOut(final float amplitude, final float period) {
		return new Elastic(amplitude, period) {

			@Override
			public final float compute(final float time) {
				float t = time;
				float a = this.a;
				final float p = this.p;
				if (t == 0F) {
					return 0F;
				}

				if ((t *= 2F) == 2F) {
					return 1F;
				}

				float s;
				if (a < 1F) {
					a = 1F;
					s = p / 4F;
				} else {
					s = p / (2F * Elastic.PI) * (float) Math.asin(1D / a);
				}

				if (t < 1F) {
					return -0.5F * (a * (float) Math.pow(2D, 10D * (t -= 1D)) * (float) Math.sin((t - s) * (2D * Elastic.PI) / p));
				}

				return a * (float) Math.pow(2D, -10D * (t -= 1D)) * (float) Math.sin((t - s) * (2D * Elastic.PI) / p) * 0.5F + 1F;
			}

			@Override
			public @NonNull Elastic a(final float a) {
				return Elastic.inOut(a, this.p);
			}

			@Override
			public @NonNull Elastic p(final float p) {
				return Elastic.inOut(this.a, p);
			}

			@Override
			public String toString() {
				return "Elastic.INOUT";
			}

		};
	}

}