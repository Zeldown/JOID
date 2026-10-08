package dev.joid.impl.vulkan.render.shader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.bridge.render.shader.source.ShaderVariable;
import dev.joid.lib.bridge.render.shader.uniform.UniformBlock;
import dev.joid.lib.bridge.render.shader.uniform.UniformMember;

public class UniformBlockLayoutTest {

	private static final String[][] MEMBERS = {
			{"float", "a", ""},
			{"vec3", "b", ""},
			{"float", "c", ""},
			{"vec2", "d", ""},
			{"ivec3", "e", ""},
			{"bool", "f", ""},
			{"mat2", "g", ""},
			{"float", "h", "[3]"},
			{"vec3", "i", "[2]"},
			{"int", "j", ""},
			{"mat3", "k", ""},
			{"vec4", "l", "[COUNT]"},
			{"mat4", "m", ""},
			{"uvec2", "n", ""},
			{"mat4", "o", "[2]"},
			{"bvec4", "p", ""},
			{"vec2", "q", "[3]"},
			{"mat3", "r", "[2]"},
			{"float", "s", ""}
	};

	@Test
	public void layoutsEveryMemberLikeTheCompiler() {
		final StringBuilder source = new StringBuilder("#version 330\n#define COUNT 5\nlayout(std140) uniform Probe {\n");
		final StringBuilder use = new StringBuilder();
		final List<ShaderVariable> variables = new ArrayList<>();
		for (final String[] member : UniformBlockLayoutTest.MEMBERS) {
			variables.add(ShaderVariable.create(member[0], member[1], member[2], false));
			source.append('\t').append(member[0]).append(' ').append(member[1]).append(member[2]).append(";\n");
			use.append(" + float(").append(member[1]).append(member[2].isEmpty() ? "" : "[0]").append(member[0].startsWith("mat") ? "[0][0]" : member[0].endsWith("vec2") || member[0].endsWith("vec3") || member[0].endsWith("vec4") ? ".x" : "").append(')');
		}
		source.append("};\nout vec4 fragColor;\nvoid main() {\n\tfragColor = vec4(0.0").append(use).append(");\n}\n");

		final UniformBlock block = UniformBlock.create(variables, "#define COUNT 5\n");
		UniformBlockLayoutTest.assertLayout(block, SpirvBlockLayout.read(GlslCompiler.compileVulkan(source.toString(), ShaderStage.FRAGMENT), "Probe"));
		UniformBlockLayoutTest.assertLayout(block, SpirvBlockLayout.read(GlslCompiler.compileOpenGl(source.toString(), ShaderStage.FRAGMENT), "Probe"));
	}

	private static void assertLayout(final UniformBlock block, final Map<String, int[]> layoutMap) {
		Assert.assertEquals(block.getMemberMap().keySet(), layoutMap.keySet());
		for (final UniformMember member : block.getMemberMap().values()) {
			Assert.assertArrayEquals(member.getName(), new int[] {member.getOffset(), member.getArrayStride(), member.getMatrixStride()}, layoutMap.get(member.getName()));
		}
	}

}