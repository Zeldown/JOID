package dev.joid.lib.utils.bezier;

import dev.joid.lib.utils.point.Point;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Bezier {

	public static @NonNull Point quadratic(final double t, final @NonNull Point start, final @NonNull Point end, final @NonNull Point control) {
		return Point.create((1 - t) * ((1 - t) * start.getX() + t * control.getX()) + t * ((1 - t) * control.getX() + t * end.getX()), (1 - t) * ((1 - t) * start.getY() + t * control.getY()) + t * ((1 - t) * control.getY() + t * end.getY()));
	}

	public static @NonNull Point cubic(final double t, final @NonNull Point start, final @NonNull Point startControl, final @NonNull Point end, final @NonNull Point endControl) {
		return Point.create(Math.pow(1 - t, 3) * start.getX() + 3 * Math.pow(1 - t, 2) * t * startControl.getX() + 3 * (1 - t) * Math.pow(t, 2) * endControl.getX() + Math.pow(t, 3) * end.getX(), Math.pow(1 - t, 3) * start.getY() + 3 * Math.pow(1 - t, 2) * t * startControl.getY() + 3 * (1 - t) * Math.pow(t, 2) * endControl.getY() + Math.pow(t, 3) * end.getY());
	}

}