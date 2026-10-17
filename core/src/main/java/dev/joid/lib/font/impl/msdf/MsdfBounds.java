package dev.joid.lib.font.impl.msdf;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MsdfBounds {

	private final float left;
	private final float bottom;
	private final float right;
	private final float top;

	public static @NonNull MsdfBounds of(final float left, final float bottom, final float right, final float top) {
		return new MsdfBounds(left, bottom, right, top);
	}

}