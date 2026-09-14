package be.zeldown.joid.lib.obj.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class OBJTextureCoordinate {

	private final float u;
	private final float v;
	private final float w;

	public OBJTextureCoordinate(final float u, final float v) {
		this(u, v, 0F);
	}

}