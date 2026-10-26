package dev.joid.lib.animation.tween;

public interface ITweenPath {

	public float compute(final float t, final float[] points, final int pointCount);

}