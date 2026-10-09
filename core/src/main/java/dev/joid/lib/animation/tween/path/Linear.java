package dev.joid.lib.animation.tween.path;



import dev.joid.lib.animation.tween.TweenPath;

public class Linear implements TweenPath {

	@Override
	public float compute(final float t, final float[] points, final int pointsCnt) {
		int segment = (int) Math.floor((pointsCnt - 1D) * t);
		segment = Math.max(segment, 0);
		segment = Math.min(segment, pointsCnt - 2);

		final float progress = t * (pointsCnt - 1F) - segment;

		return points[segment] + progress * (points[segment + 1] - points[segment]);
	}

}