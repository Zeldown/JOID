package be.zeldown.joid.lib.animation.tweenengine;

public interface TweenPath {

	public float compute(float t, float[] points, int pointsCnt);

}