package be.zeldown.joid.lib.animation.tweenengine.equation;



import be.zeldown.joid.lib.animation.tweenengine.TweenEquation;

public abstract class Elastic extends TweenEquation {

	private static final float PI = 3.14159265F;

	public static final Elastic IN = new Elastic() {

		@Override
		public final float compute(float t) {
			float a = this.a;
			float p = this.p;
			if (t == 0F) {
				return 0F;
			}

			if (t == 1F) {
				return 1F;
			}

			if (!this.setP) {
				p = 0.3F;
			}

			float s;
			if (!this.setA || a < 1F) {
				a = 1F;
				s = p / 4F;
			} else {
				s = p / (2F * Elastic.PI) * (float) Math.asin(1F / a);
			}

			return -(a * (float) Math.pow(2D, 10D * (t -= 1D)) * (float) Math.sin((t - s) * (2D * Elastic.PI) / p));
		}

		@Override
		public String toString() {
			return "Elastic.IN";
		}

	};

	public static final Elastic OUT = new Elastic() {

		@Override
		public final float compute(final float t) {
			float a = this.a;
			float p = this.p;
			if (t == 0F) {
				return 0F;
			}

			if (t == 1F) {
				return 1F;
			}

			if (!this.setP) {
				p = 0.3F;
			}

			float s;
			if (!this.setA || a < 1F) {
				a = 1F;
				s = p / 4F;
			} else {
				s = p / (2F * Elastic.PI) * (float) Math.asin(1F / a);
			}

			return a * (float) Math.pow(2D, -10D * t) * (float) Math.sin((t - s) * (2D * Elastic.PI) / p) + 1F;
		}

		@Override
		public String toString() {
			return "Elastic.OUT";
		}

	};

	public static final Elastic INOUT = new Elastic() {

		@Override
		public final float compute(float t) {
			float a = this.a;
			float p = this.p;
			if (t == 0F) {
				return 0F;
			}

			if ((t *= 2F) == 2F) {
				return 1F;
			}

			if (!this.setP) {
				p = 0.3F * 1.5F;
			}

			float s;
			if (!this.setA || a < 1F) {
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
		public String toString() {
			return "Elastic.INOUT";
		}

	};

	// -------------------------------------------------------------------------

	protected float a;
	protected float p;
	protected boolean setA = false;
	protected boolean setP = false;

	public Elastic a(final float a) {
		this.a = a;
		this.setA = true;
		return this;
	}

	public Elastic p(final float p) {
		this.p = p;
		this.setP = true;
		return this;
	}

}