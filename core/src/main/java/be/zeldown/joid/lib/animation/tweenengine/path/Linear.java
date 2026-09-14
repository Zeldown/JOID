package be.zeldown.joid.lib.animation.tweenengine.path;



import be.zeldown.joid.lib.animation.tweenengine.TweenPath;

public class Linear implements TweenPath {

	@Override
	public float compute(float t, final float[] points, final int pointsCnt) {
		int segment = (int) Math.floor((pointsCnt - 1D) * t);
		segment = Math.max(segment, 0);
		segment = Math.min(segment, pointsCnt - 2);

		t = t * (pointsCnt - 1F) - segment;

		return points[segment] + t * (points[segment + 1] - points[segment]);
	}

}
