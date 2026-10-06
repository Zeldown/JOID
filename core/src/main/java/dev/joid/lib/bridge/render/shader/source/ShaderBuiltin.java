package dev.joid.lib.bridge.render.shader.source;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ShaderBuiltin {

	POSITION("aPosition", "vec3", Kind.ATTRIBUTE),
	TEXTURE_COORDINATE("aTexCoord", "vec2", Kind.ATTRIBUTE),
	COLOR("aColor", "vec4", Kind.ATTRIBUTE),
	NORMAL("aNormal", "vec3", Kind.ATTRIBUTE),
	PROJECTION_MATRIX("uProjectionMatrix", "mat4", Kind.UNIFORM),
	MODEL_VIEW_MATRIX("uModelViewMatrix", "mat4", Kind.UNIFORM),
	NORMAL_MATRIX("uNormalMatrix", "mat3", Kind.UNIFORM),
	LIGHTING("uLighting", "bool", Kind.UNIFORM),
	FRAGMENT_COLOR("fragColor", "vec4", Kind.OUTPUT);

	private final String identifier;
	private final String type;
	private final Kind   kind;

	public static ShaderBuiltin find(final @NonNull String identifier) {
		for (final ShaderBuiltin builtin : ShaderBuiltin.values()) {
			if (builtin.getIdentifier().equals(identifier)) {
				return builtin;
			}
		}
		return null;
	}

	public enum Kind {

		ATTRIBUTE,
		UNIFORM,
		OUTPUT;

	}

}