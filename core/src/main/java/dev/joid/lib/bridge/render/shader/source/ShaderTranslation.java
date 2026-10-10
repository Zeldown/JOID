package dev.joid.lib.bridge.render.shader.source;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderTranslation {

	private final GlslDialect dialect;
	private final String      vertex;
	private final String      fragment;
	private final String      stencilFragment;

	public static @NonNull ShaderTranslation of(final @NonNull GlslDialect dialect, final @NonNull String vertex, final @NonNull String fragment, final String stencilFragment) {
		return new ShaderTranslation(dialect, vertex, fragment, stencilFragment);
	}

}