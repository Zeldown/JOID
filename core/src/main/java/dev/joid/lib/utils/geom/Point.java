package dev.joid.lib.utils.geom;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Point {

	private final double x;
	private final double y;
	private final double z;

	public static @NonNull Point create(final double x, final double y) {
		return new Point(x, y, 0D);
	}

	public static @NonNull Point create(final double x, final double y, final double z) {
		return new Point(x, y, z);
	}

}