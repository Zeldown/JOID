package be.zeldown.joid.lib.obj.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class OBJVertex {

	private final float x;
	private final float y;
	private final float z;

	public OBJVertex(final float x, final float y) {
		this(x, y, 0F);
	}

}