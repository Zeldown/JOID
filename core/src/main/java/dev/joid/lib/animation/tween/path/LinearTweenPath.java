package dev.joid.lib.animation.tween.path;

import dev.joid.lib.animation.tween.ITweenPath;
import lombok.NonNull;

public class LinearTweenPath implements ITweenPath {

	@Override
	public float compute(final float t, final @NonNull float[] points, final int pointCount) {
		int segment = (int) Math.floor((pointCount - 1D) * t);
		segment = Math.max(segment, 0);
		segment = Math.min(segment, pointCount - 2);

		final float progress = t * (pointCount - 1F) - segment;

		return points[segment] + progress * (points[segment + 1] - points[segment]);
	}

}