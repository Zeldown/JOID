package dev.joid.lib.obj.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjTextureCoordinate {

	private final float u;
	private final float v;
	private final float w;

	public static @NonNull ObjTextureCoordinate of(final float u, final float v) {
		return new ObjTextureCoordinate(u, v, 0F);
	}

	public static @NonNull ObjTextureCoordinate of(final float u, final float v, final float w) {
		return new ObjTextureCoordinate(u, v, w);
	}

}