package dev.joid.backend.vulkan.render.shader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

import org.lwjgl.util.shaderc.Shaderc;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VulkanShaderCompiler {

	private static long compiler;

	public static ByteBuffer compile(final String source, final ShaderStage stage) {
		if (VulkanShaderCompiler.compiler == 0L) {
			VulkanShaderCompiler.compiler = Shaderc.shaderc_compiler_initialize();
		}

		final String name = stage.name().toLowerCase(Locale.ROOT);
		final int kind = stage == ShaderStage.VERTEX ? Shaderc.shaderc_vertex_shader : Shaderc.shaderc_fragment_shader;
		final long result = Shaderc.shaderc_compile_into_spv(VulkanShaderCompiler.compiler, source, kind, name, "main", 0L);
		try {
			if (Shaderc.shaderc_result_get_compilation_status(result) != Shaderc.shaderc_compilation_status_success) {
				JOIDLog.warn("The Vulkan " + name + " shader cannot be compiled, fix its source at the line shaderc reports: " + Shaderc.shaderc_result_get_error_message(result));
				return null;
			}

			final ByteBuffer bytes = Shaderc.shaderc_result_get_bytes(result);
			final ByteBuffer code = ByteBuffer.allocateDirect(bytes.remaining()).order(ByteOrder.nativeOrder());
			code.put(bytes).flip();
			return code;
		} finally {
			Shaderc.shaderc_result_release(result);
		}
	}

}