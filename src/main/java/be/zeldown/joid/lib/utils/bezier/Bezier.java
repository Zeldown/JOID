package be.zeldown.joid.lib.utils.bezier;

import javax.vecmath.Vector2d;

import lombok.NonNull;

/**
 * The Bezier class in Java provides methods for calculating points on quadratic and cubic Bezier
 * curves.
 */
public class Bezier {

	/**
	 * The function calculates a quadratic Bezier curve point at parameter t using start, end, and control
	 * vectors.
	 * 
	 * @param t The parameter `t` in the `quadratic` method represents the interpolation value between the
	 * start and end points of a quadratic Bezier curve. It typically ranges from 0 to 1, where 0
	 * corresponds to the start point and 1 corresponds to the end point of the curve. Values
	 * @param start The `start` parameter represents the starting point of the quadratic Bezier curve in
	 * 2D space.
	 * @param end The `end` parameter in the `quadratic` method represents the end point of a quadratic
	 * Bezier curve. It is a `Vector2d` object that specifies the x and y coordinates of the end point of
	 * the curve.
	 * @param control The `control` parameter in the `quadratic` method represents the control point in a
	 * quadratic Bézier curve. In a quadratic Bézier curve, the curve is defined by a start point, an end
	 * point, and a single control point that influences the shape of the curve.
	 * @return The method `quadratic` returns a new `Vector2d` object that represents a point on a
	 * quadratic Bezier curve at a given parameter `t` between the start point, end point, and control
	 * point provided as arguments.
	 */
	public static @NonNull Vector2d quadratic(final double t, final @NonNull Vector2d start, final @NonNull Vector2d end, final @NonNull Vector2d control) {
		return new Vector2d((1 - t) * ((1 - t) * start.x + t * control.x) + t * ((1 - t) * control.x + t * end.x), (1 - t) * ((1 - t) * start.y + t * control.y) + t * ((1 - t) * control.y + t * end.y));
	}

	/**
	 * This Java function calculates a point on a cubic Bezier curve given a parameter value and control
	 * points.
	 * 
	 * @param t The parameter `t` represents the interpolation value between 0 and 1 for the cubic Bezier
	 * curve calculation. It determines the position along the curve where you want to calculate the
	 * corresponding point.
	 * @param start The `start` parameter represents the starting point of the cubic Bezier curve. It is a
	 * `Vector2d` object that contains the x and y coordinates of the starting point.
	 * @param startControl The `startControl` parameter in the `cubic` method represents the control point
	 * that influences the direction and curvature of the curve at the start of the cubic Bezier curve
	 * segment. It is used in conjunction with the `start` and `end` points to define the shape of the
	 * curve.
	 * @param end The `end` parameter in the `cubic` method represents the end point of the cubic Bezier
	 * curve. It is a Vector2d object that defines the final position of the curve.
	 * @param endControl The `endControl` parameter in the `cubic` method represents the control point
	 * that influences the direction and curvature of the curve towards the end point of the cubic Bézier
	 * curve. It helps in defining the shape of the curve between the start and end points.
	 * @return The method is returning a new Vector2d object that represents a point on a cubic Bezier
	 * curve at a given parameter value 't'. The position of the point is calculated based on the control
	 * points provided (start, startControl, end, endControl) using the cubic Bezier curve formula.
	 */
	public static @NonNull Vector2d cubic(final double t, final @NonNull Vector2d start, final @NonNull Vector2d startControl, final @NonNull Vector2d end, final @NonNull Vector2d endControl) {
		return new Vector2d(Math.pow(1 - t, 3) * start.x + 3 * Math.pow(1 - t, 2) * t * startControl.x + 3 * (1 - t) * Math.pow(t, 2) * endControl.x + Math.pow(t, 3) * end.x, Math.pow(1 - t, 3) * start.y + 3 * Math.pow(1 - t, 2) * t * startControl.y + 3 * (1 - t) * Math.pow(t, 2) * endControl.y + Math.pow(t, 3) * end.y);
	}

}