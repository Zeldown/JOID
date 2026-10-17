package dev.joid.lib.utils.geom;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Bezier {

	public static @NonNull Point quadratic(final double t, final @NonNull Point start, final @NonNull Point control, final @NonNull Point end) {
		return Point.of((1D - t) * ((1D - t) * start.getX() + t * control.getX()) + t * ((1D - t) * control.getX() + t * end.getX()), (1D - t) * ((1D - t) * start.getY() + t * control.getY()) + t * ((1D - t) * control.getY() + t * end.getY()));
	}

	public static @NonNull Point cubic(final double t, final @NonNull Point start, final @NonNull Point startControl, final @NonNull Point endControl, final @NonNull Point end) {
		return Point.of(Math.pow(1D - t, 3D) * start.getX() + 3D * Math.pow(1D - t, 2D) * t * startControl.getX() + 3D * (1D - t) * Math.pow(t, 2D) * endControl.getX() + Math.pow(t, 3D) * end.getX(), Math.pow(1D - t, 3D) * start.getY() + 3D * Math.pow(1D - t, 2D) * t * startControl.getY() + 3D * (1D - t) * Math.pow(t, 2D) * endControl.getY() + Math.pow(t, 3D) * end.getY());
	}

}