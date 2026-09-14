package be.zeldown.joid.lib.bridge.render.shader;

import be.zeldown.joid.lib.bridge.render.shader.uniform.BooleanUniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float2Uniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float3Uniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float4ArrayUniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float4Uniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.FloatArrayUniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.FloatMatrixUniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.FloatUniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.IntUniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.SamplerUniform;
import lombok.NonNull;

public interface IShader {

	public void bind();
	public void unbind();

	public boolean isActive();
	public boolean isBound();

	public @NonNull SamplerUniform getSamplerUniform(final @NonNull String name);
	public @NonNull BooleanUniform getBooleanUniform(final @NonNull String name);
	public @NonNull IntUniform getIntUniform(final @NonNull String name);
	public @NonNull FloatArrayUniform getFloatArrayUniform(final @NonNull String name);
	public @NonNull FloatUniform getFloatUniform(final @NonNull String name);
	public @NonNull Float2Uniform getFloat2Uniform(final @NonNull String name);
	public @NonNull Float3Uniform getFloat3Uniform(final @NonNull String name);
	public @NonNull Float4Uniform getFloat4Uniform(final @NonNull String name);
	public @NonNull FloatMatrixUniform getFloatMatrixUniform(final @NonNull String name);
	public @NonNull Float4ArrayUniform getFloat4ArrayUniform(final @NonNull String name);

}