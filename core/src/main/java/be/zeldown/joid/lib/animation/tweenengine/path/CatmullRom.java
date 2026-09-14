package be.zeldown.joid.lib.animation.tweenengine.path;



import be.zeldown.joid.lib.animation.tweenengine.TweenPath;

public class CatmullRom implements TweenPath {

	@Override
	public float compute(float t, final float[] points, final int pointsCnt) {
		int segment = (int) Math.floor((pointsCnt - 1D) * t);
		segment = Math.max(segment, 0);
		segment = Math.min(segment, pointsCnt - 2);

		t = t * (pointsCnt - 1F) - segment;

		if (segment == 0F) {
			return this.catmullRomSpline(points[0], points[0], points[1], points[2], t);
		}

		if (segment == pointsCnt - 2F) {
			return this.catmullRomSpline(points[pointsCnt - 3], points[pointsCnt - 2], points[pointsCnt - 1], points[pointsCnt - 1], t);
		}

		return this.catmullRomSpline(points[segment - 1], points[segment], points[segment + 1], points[segment + 2], t);
	}

	private float catmullRomSpline(final float a, final float b, final float c, final float d, final float t) {
		final float t1 = (c - a) * 0.5F;
		final float t2 = (d - b) * 0.5F;

		final float h1 = +2F * t * t * t - 3F * t * t + 1F;
		final float h2 = -2F * t * t * t + 3F * t * t;
		final float h3 = t * t * t - 2F * t * t + t;
		final float h4 = t * t * t - t * t;

		return b * h1 + c * h2 + t1 * h3 + t2 * h4;
	}

}