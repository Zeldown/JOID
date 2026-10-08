package dev.joid.lib.bridge.render.shader;

import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureSampling;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SamplerBinding {

	private final ITexture        texture;
	private final TextureSampling sampling;

	public static @NonNull SamplerBinding of(final @NonNull ITexture texture, final @NonNull TextureSampling sampling) {
		return new SamplerBinding(texture, sampling);
	}

}