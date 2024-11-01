package be.zeldown.joid.lib.animation.tweenengine;

public interface TweenPath {

	float compute(float t, float[] points, int pointsCnt);

}