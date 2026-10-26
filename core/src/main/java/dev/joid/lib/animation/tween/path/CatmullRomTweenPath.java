package dev.joid.lib.animation.tween.path;

import dev.joid.lib.animation.tween.ITweenPath;
import lombok.NonNull;

public class CatmullRomTweenPath implements ITweenPath {

	@Override
	public float compute(final float t, final @NonNull float[] points, final int pointCount) {
		int segment = (int) Math.floor((pointCount - 1D) * t);
		segment = Math.max(segment, 0);
		segment = Math.min(segment, pointCount - 2);

		final float progress = t * (pointCount - 1F) - segment;

		if (segment == 0) {
			return this.catmullRomSpline(points[0], points[0], points[1], points[Math.min(2, pointCount - 1)], progress);
		}

		if (segment == pointCount - 2) {
			return this.catmullRomSpline(points[pointCount - 3], points[pointCount - 2], points[pointCount - 1], points[pointCount - 1], progress);
		}

		return this.catmullRomSpline(points[segment - 1], points[segment], points[segment + 1], points[segment + 2], progress);
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