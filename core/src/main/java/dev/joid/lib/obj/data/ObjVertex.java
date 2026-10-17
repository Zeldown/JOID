package dev.joid.lib.obj.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjVertex {

	private final float x;
	private final float y;
	private final float z;

	public static @NonNull ObjVertex of(final float x, final float y) {
		return new ObjVertex(x, y, 0F);
	}

	public static @NonNull ObjVertex of(final float x, final float y, final float z) {
		return new ObjVertex(x, y, z);
	}

}