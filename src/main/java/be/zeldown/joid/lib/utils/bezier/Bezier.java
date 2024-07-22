package be.zeldown.joid.lib.utils.bezier;

import javax.vecmath.Vector2d;

public class Bezier {

	public static Vector2d quadratic(final double t, final Vector2d start, final Vector2d end, final Vector2d control) {
		return new Vector2d((1 - t) * ((1 - t) * start.x + t * control.x) + t * ((1 - t) * control.x + t * end.x), (1 - t) * ((1 - t) * start.y + t * control.y) + t * ((1 - t) * control.y + t * end.y));
	}

	public static Vector2d cubic(final double t, final Vector2d start, final Vector2d startControl, final Vector2d end, final Vector2d endControl) {
		return new Vector2d(Math.pow(1 - t, 3) * start.x + 3 * Math.pow(1 - t, 2) * t * startControl.x + 3 * (1 - t) * Math.pow(t, 2) * endControl.x + Math.pow(t, 3) * end.x, Math.pow(1 - t, 3) * start.y + 3 * Math.pow(1 - t, 2) * t * startControl.y + 3 * (1 - t) * Math.pow(t, 2) * endControl.y + Math.pow(t, 3) * end.y);
	}

}