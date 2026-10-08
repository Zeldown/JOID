package dev.joid.test.shader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.util.shaderc.Shaderc;

import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlslCompiler {

	public static @NonNull ByteBuffer compileVulkan(final @NonNull String source, final @NonNull ShaderStage stage) {
		return GlslCompiler.compile(source, stage, Shaderc.shaderc_target_env_vulkan, Shaderc.shaderc_env_version_vulkan_1_2);
	}

	public static @NonNull ByteBuffer compileOpenGl(final @NonNull String source, final @NonNull ShaderStage stage) {
		return GlslCompiler.compile(source, stage, Shaderc.shaderc_target_env_opengl, Shaderc.shaderc_env_version_opengl_4_5);
	}

	private static ByteBuffer compile(final String source, final ShaderStage stage, final int environment, final int version) {
		final long compiler = Shaderc.shaderc_compiler_initialize();
		final long options = Shaderc.shaderc_compile_options_initialize();
		try {
			Shaderc.shaderc_compile_options_set_target_env(options, environment, version);
			Shaderc.shaderc_compile_options_set_auto_bind_uniforms(options, true);
			Shaderc.shaderc_compile_options_set_auto_map_locations(options, true);
			Shaderc.shaderc_compile_options_set_optimization_level(options, Shaderc.shaderc_optimization_level_zero);

			final int kind = stage == ShaderStage.VERTEX ? Shaderc.shaderc_vertex_shader : Shaderc.shaderc_fragment_shader;
			final long result = Shaderc.shaderc_compile_into_spv(compiler, source, kind, stage.name().toLowerCase(), "main", options);
			try {
				if (Shaderc.shaderc_result_get_compilation_status(result) != Shaderc.shaderc_compilation_status_success) {
					throw new AssertionError(Shaderc.shaderc_result_get_error_message(result) + "\n" + source);
				}

				final ByteBuffer bytes = Shaderc.shaderc_result_get_bytes(result);
				final ByteBuffer code = ByteBuffer.allocate(bytes.remaining()).order(ByteOrder.LITTLE_ENDIAN);
				code.put(bytes).flip();
				return code;
			} finally {
				Shaderc.shaderc_result_release(result);
			}
		} finally {
			Shaderc.shaderc_compile_options_release(options);
			Shaderc.shaderc_compiler_release(compiler);
		}
	}

}