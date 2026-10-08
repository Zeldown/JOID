package dev.joid.lib.bridge.render.shader.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Locale;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.state.BlendState;
import lombok.NonNull;

public enum CoreShader {

	BLUR,
	BORDER,
	CIRCLE,
	FIXED,
	FONT,
	GRADIENT,
	LINE,
	ROUNDED,
	SHADOW;

	public @NonNull InputStream open(final @NonNull ShaderStage stage) {
		final String name = this.name().toLowerCase(Locale.ROOT);
		final InputStream stream = CoreShader.class.getResourceAsStream("/assets/shaders/" + name + "/" + name + (stage == ShaderStage.VERTEX ? ".vsh" : ".fsh"));
		if (stream == null) {
			throw new IllegalStateException("The core shader " + name + " is missing from the classpath");
		}
		return stream;
	}

	public @NonNull ShaderSource read(final @NonNull ShaderStage stage) {
		try (InputStream stream = this.open(stage)) {
			return ShaderSource.read(stage, stream);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public @NonNull IShader create(final @NonNull BlendState blend) {
		return BridgeHandler.RENDER.get().createShader(this.read(ShaderStage.VERTEX), this.read(ShaderStage.FRAGMENT), blend);
	}

}