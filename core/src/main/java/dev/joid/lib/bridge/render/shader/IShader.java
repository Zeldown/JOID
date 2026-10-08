package dev.joid.lib.bridge.render.shader;

import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import lombok.NonNull;

public interface IShader {

	public void bind();
	public void unbind();

	public boolean isBound();
	public boolean isActive();

	public @NonNull IShader uniform(final @NonNull String name, final int value);
	public @NonNull IShader uniform(final @NonNull String name, final boolean value);
	public @NonNull IShader uniform(final @NonNull String name, final @NonNull float... values);

	public @NonNull IShader sampler(final @NonNull String name, final @NonNull ITexture texture, final @NonNull TextureFilter filter, final @NonNull TextureWrap wrap);

}