package dev.joid.lib.animation.tweenengine;

public interface TweenPath {

	public float compute(final float t, final float[] points, final int pointsCnt);

}