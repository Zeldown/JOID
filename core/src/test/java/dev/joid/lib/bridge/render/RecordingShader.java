package dev.joid.lib.bridge.render;

import java.util.HashMap;
import java.util.Map;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class RecordingShader implements IShader {

	private final Map<String, Object> values = new HashMap<>();

	private boolean bound;

	@Override
	public void bind() {
		BridgeHandler.RENDER.get().shader(this);
		this.bound = true;
	}

	@Override
	public void unbind() {
		BridgeHandler.RENDER.get().shader(null);
		this.bound = false;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public @NonNull IShader uniform(final @NonNull String name, final int value) {
		this.values.put(name, value);
		return this;
	}

	@Override
	public @NonNull IShader uniform(final @NonNull String name, final boolean value) {
		this.values.put(name, value);
		return this;
	}

	@Override
	public @NonNull IShader uniform(final @NonNull String name, final @NonNull float... values) {
		this.values.put(name, values.length == 1 ? (Object) values[0] : values.clone());
		return this;
	}

	@Override
	public @NonNull IShader sampler(final @NonNull String name, final @NonNull ITexture texture, final @NonNull TextureFilter filter, final @NonNull TextureWrap wrap) {
		this.values.put(name, texture);
		return this;
	}

}