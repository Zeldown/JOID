package dev.joid.lib.bridge.render;

import java.util.HashMap;
import java.util.Map;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.uniform.BooleanUniform;
import dev.joid.lib.bridge.render.shader.uniform.Float2Uniform;
import dev.joid.lib.bridge.render.shader.uniform.Float3Uniform;
import dev.joid.lib.bridge.render.shader.uniform.Float4ArrayUniform;
import dev.joid.lib.bridge.render.shader.uniform.Float4Uniform;
import dev.joid.lib.bridge.render.shader.uniform.FloatArrayUniform;
import dev.joid.lib.bridge.render.shader.uniform.FloatMatrixUniform;
import dev.joid.lib.bridge.render.shader.uniform.FloatUniform;
import dev.joid.lib.bridge.render.shader.uniform.IntUniform;
import dev.joid.lib.bridge.render.shader.uniform.SamplerUniform;
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
	public @NonNull IntUniform getIntUniform(final @NonNull String name) {
		return value -> this.values.put(name, value);
	}

	@Override
	public @NonNull FloatUniform getFloatUniform(final @NonNull String name) {
		return value -> this.values.put(name, value);
	}

	@Override
	public @NonNull Float2Uniform getFloat2Uniform(final @NonNull String name) {
		return (f1, f2) -> this.values.put(name, new float[] {f1, f2});
	}

	@Override
	public @NonNull Float3Uniform getFloat3Uniform(final @NonNull String name) {
		return (f1, f2, f3) -> this.values.put(name, new float[] {f1, f2, f3});
	}

	@Override
	public @NonNull Float4Uniform getFloat4Uniform(final @NonNull String name) {
		return (f1, f2, f3, f4) -> this.values.put(name, new float[] {f1, f2, f3, f4});
	}

	@Override
	public @NonNull BooleanUniform getBooleanUniform(final @NonNull String name) {
		return value -> this.values.put(name, value);
	}

	@Override
	public @NonNull SamplerUniform getSamplerUniform(final @NonNull String name) {
		return (texture, filter, wrap) -> this.values.put(name, texture);
	}

	@Override
	public @NonNull FloatArrayUniform getFloatArrayUniform(final @NonNull String name) {
		return array -> this.values.put(name, array.clone());
	}

	@Override
	public @NonNull Float4ArrayUniform getFloat4ArrayUniform(final @NonNull String name) {
		return array -> this.values.put(name, array.clone());
	}

	@Override
	public @NonNull FloatMatrixUniform getFloatMatrixUniform(final @NonNull String name) {
		return matrix -> this.values.put(name, matrix.clone());
	}

}