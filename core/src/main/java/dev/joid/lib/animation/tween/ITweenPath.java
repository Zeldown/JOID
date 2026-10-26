package dev.joid.lib.animation.tween;

import lombok.NonNull;

public interface ITweenPath {

	public float compute(final float t, final @NonNull float[] points, final int pointCount);

}