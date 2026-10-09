package dev.joid.lib.bridge.render.shader.source;

import java.io.InputStream;
import java.util.Locale;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.state.BlendState;
import lombok.NonNull;

public enum CoreShader {

	BITMAP,
	BLUR,
	BORDER,
	CIRCLE,
	DEFAULT,
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
		return ShaderSource.read(stage, this.open(stage));
	}

	public @NonNull IShader create(final @NonNull BlendState blend) {
		return BridgeHandler.RENDER.get().createShader(this.read(ShaderStage.VERTEX), this.read(ShaderStage.FRAGMENT), blend);
	}

}