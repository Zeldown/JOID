package be.zeldown.joid.impl.vulkan.render.shader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.util.shaderc.Shaderc;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderCompiler {

	private static final String ALPHA_TEST_MAIN = "\nlayout(std140, binding = 15) uniform JoidAlphaTest {\n\tint joid_AlphaTest;\n\tfloat joid_AlphaThreshold;\n};\n\nvoid main() {\n\tjoid_main();\n\tif (joid_AlphaTest != 0 && fragColor.a <= joid_AlphaThreshold) {\n\t\tdiscard;\n\t}\n}\n";

	private static long compiler;

	public static @NonNull ByteBuffer compileVertex(final @NonNull String source) {
		return ShaderCompiler.compile(source, Shaderc.shaderc_vertex_shader, "vertex");
	}

	public static @NonNull ByteBuffer compileFragment(final @NonNull String source) {
		final String alphaTestedSource = source.replaceFirst("void\\s+main\\s*\\(\\s*\\)", "void joid_main()") + ShaderCompiler.ALPHA_TEST_MAIN;
		return ShaderCompiler.compile(alphaTestedSource, Shaderc.shaderc_fragment_shader, "fragment");
	}

	private static ByteBuffer compile(final String source, final int kind, final String name) {
		if (ShaderCompiler.compiler == 0L) {
			ShaderCompiler.compiler = Shaderc.shaderc_compiler_initialize();
		}

		final long result = Shaderc.shaderc_compile_into_spv(ShaderCompiler.compiler, source, kind, name, "main", 0L);
		try {
			if (Shaderc.shaderc_result_get_compilation_status(result) != Shaderc.shaderc_compilation_status_success) {
				throw new IllegalStateException("Vulkan " + name + " shader compilation failed: " + Shaderc.shaderc_result_get_error_message(result));
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