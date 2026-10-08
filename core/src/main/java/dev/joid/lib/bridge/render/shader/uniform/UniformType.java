package dev.joid.lib.bridge.render.shader.uniform;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UniformType {

	INT("int", 1, 0, true),
	UINT("uint", 1, 0, true),
	BOOL("bool", 1, 0, true),
	FLOAT("float", 1, 0, false),
	VEC2("vec2", 2, 0, false),
	VEC3("vec3", 3, 0, false),
	VEC4("vec4", 4, 0, false),
	IVEC2("ivec2", 2, 0, true),
	IVEC3("ivec3", 3, 0, true),
	IVEC4("ivec4", 4, 0, true),
	UVEC2("uvec2", 2, 0, true),
	UVEC3("uvec3", 3, 0, true),
	UVEC4("uvec4", 4, 0, true),
	BVEC2("bvec2", 2, 0, true),
	BVEC3("bvec3", 3, 0, true),
	BVEC4("bvec4", 4, 0, true),
	MAT2("mat2", 2, 2, false),
	MAT3("mat3", 3, 3, false),
	MAT4("mat4", 4, 4, false);

	private final String  identifier;
	private final int     components;
	private final int     columns;
	private final boolean integer;

	public static @NonNull UniformType of(final @NonNull String identifier) {
		for (final UniformType type : UniformType.values()) {
			if (type.getIdentifier().equals(identifier)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unsupported uniform type " + identifier);
	}

	public boolean isMatrix() {
		return this.columns > 0;
	}

	public int getCount() {
		return this.components * Math.max(this.columns, 1);
	}

}