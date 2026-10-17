package dev.joid.backend.vulkan.render.shader;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.shader.source.ShaderStage;

public class VulkanShaderCompilerTest {

	@Test
	public void warnsAndGivesNoCodeForASourceThatDoesNotCompile() {
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setErr(new PrintStream(output, true));
		try {
			Assert.assertNull(VulkanShaderCompiler.compile("#version 450\nvoid main() {\n    broken;\n}\n", ShaderStage.FRAGMENT));
		} finally {
			System.setErr(previous);
		}
		Assert.assertTrue(output.toString(), output.toString().contains("[JOID] The Vulkan fragment shader cannot be compiled, fix its source at the line shaderc reports: "));
	}

}