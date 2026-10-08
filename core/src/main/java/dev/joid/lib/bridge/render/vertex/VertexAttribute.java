package dev.joid.lib.bridge.render.vertex;

import dev.joid.lib.bridge.render.shader.source.ShaderBuiltin;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum VertexAttribute {

	POSITION(ShaderBuiltin.POSITION, 0, 0, 3, VertexComponent.FLOAT, false),
	TEXTURE_COORDINATE(ShaderBuiltin.TEXTURE_COORDINATE, 1, 12, 2, VertexComponent.FLOAT, false),
	COLOR(ShaderBuiltin.COLOR, 2, 20, 4, VertexComponent.UNSIGNED_BYTE, true),
	NORMAL(ShaderBuiltin.NORMAL, 3, 24, 3, VertexComponent.BYTE, true);

	private final ShaderBuiltin   builtin;
	private final int             location;
	private final int             offset;
	private final int             components;
	private final VertexComponent component;
	private final boolean         normalized;

	public static VertexAttribute of(final @NonNull ShaderBuiltin builtin) {
		for (final VertexAttribute attribute : VertexAttribute.values()) {
			if (attribute.getBuiltin() == builtin) {
				return attribute;
			}
		}
		return null;
	}

}