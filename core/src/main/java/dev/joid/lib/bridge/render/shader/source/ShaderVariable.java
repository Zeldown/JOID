package dev.joid.lib.bridge.render.shader.source;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderVariable {

	private final String  type;
	private final String  name;
	private final String  array;
	private final boolean flat;

	public static @NonNull ShaderVariable create(final @NonNull String type, final @NonNull String name, final @NonNull String array, final boolean flat) {
		return new ShaderVariable(type, name, array, flat);
	}

	public @NonNull String getDeclaration() {
		return this.type + " " + this.name + this.array;
	}

}