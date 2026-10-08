package dev.joid.lib.bridge.render.shader.uniform;

import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UniformSampler {

	private final String name;
	private final int    unit;

	private ITexture      texture;
	private TextureWrap   wrap;
	private TextureFilter filter;

	public static @NonNull UniformSampler create(final @NonNull String name, final int unit) {
		return new UniformSampler(name, unit);
	}

	public @NonNull UniformSampler value(final @NonNull ITexture texture, final @NonNull TextureFilter filter, final @NonNull TextureWrap wrap) {
		this.texture = texture;
		this.filter  = filter;
		this.wrap    = wrap;
		return this;
	}

}