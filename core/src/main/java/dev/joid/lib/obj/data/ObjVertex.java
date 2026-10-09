package dev.joid.lib.obj.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class ObjVertex {

	private final float x;
	private final float y;
	private final float z;

	public ObjVertex(final float x, final float y) {
		this(x, y, 0F);
	}

}