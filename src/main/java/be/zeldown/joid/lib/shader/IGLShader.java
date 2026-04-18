package be.zeldown.joid.lib.shader;

import be.zeldown.joid.lib.shader.uniform.BooleanUniform;
import be.zeldown.joid.lib.shader.uniform.Float2Uniform;
import be.zeldown.joid.lib.shader.uniform.Float3Uniform;
import be.zeldown.joid.lib.shader.uniform.Float4ArrayUniform;
import be.zeldown.joid.lib.shader.uniform.Float4Uniform;
import be.zeldown.joid.lib.shader.uniform.FloatArrayUniform;
import be.zeldown.joid.lib.shader.uniform.FloatMatrixUniform;
import be.zeldown.joid.lib.shader.uniform.FloatUniform;
import be.zeldown.joid.lib.shader.uniform.IntUniform;
import be.zeldown.joid.lib.shader.uniform.SamplerUniform;
import lombok.NonNull;

public interface IGLShader {

	void bind();
	void unbind();

	boolean isActive();
	boolean isBound();
	boolean supportsTransformations();

	void captureCurrentTransformations();
	void updateModelMatrix(@NonNull float[] matrix);
	void updateViewMatrix(@NonNull float[] matrix);
	void updateProjectionMatrix(@NonNull float[] matrix);

	void bindTexture(int textureUnit, int textureId);

	@NonNull SamplerUniform getSamplerUniform(final @NonNull String name);
	@NonNull BooleanUniform getBooleanUniform(final @NonNull String name);
	@NonNull IntUniform getIntUniform(final @NonNull String name);
	@NonNull FloatArrayUniform getFloatArrayUniform(final @NonNull String name);
	@NonNull FloatUniform getFloatUniform(final @NonNull String name);
	@NonNull Float2Uniform getFloat2Uniform(final @NonNull String name);
	@NonNull Float3Uniform getFloat3Uniform(final @NonNull String name);
	@NonNull Float4Uniform getFloat4Uniform(final @NonNull String name);
	@NonNull FloatMatrixUniform getFloatMatrixUniform(final @NonNull String name);
	@NonNull Float4ArrayUniform getFloat4ArrayUniform(final @NonNull String name);

}