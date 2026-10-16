package dev.joid.backend.vulkan.render.shader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.util.shaderc.Shaderc;

import dev.joid.internal.JOIDLog;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VulkanShaderCompiler {

	private static long compiler;

	public static ByteBuffer compileVertex(final String source) {
		return VulkanShaderCompiler.compile(source, Shaderc.shaderc_vertex_shader, "vertex");
	}

	public static ByteBuffer compileFragment(final String source) {
		return VulkanShaderCompiler.compile(source, Shaderc.shaderc_fragment_shader, "fragment");
	}

	private static ByteBuffer compile(final String source, final int kind, final String name) {
		if (VulkanShaderCompiler.compiler == 0L) {
			VulkanShaderCompiler.compiler = Shaderc.shaderc_compiler_initialize();
		}

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